/*
 * posts/write.js
 * - 글 작성 화면의 라이브 미리보기/툴바 처리 및 파일/이미지 업로드 프리뷰
 * - 임시 업로드 이미지 추적 및 정리, Kakao Maps SDK 동적 주입
 */

// Kakao Maps SDK 동적 주입
(function () {
  var metaEl = document.querySelector('meta[name="kakao-js-key"]');
  var key = metaEl ? metaEl.getAttribute('content') : '';
  if (!key) {
    console.error('[Kakao SDK] kakaoJsKey is empty. Set property kakao.maps.javascript.key');
    return;
  }
  var url = 'https://dapi.kakao.com/v2/maps/sdk.js'
    + '?appkey=' + encodeURIComponent(key)
    + '&libraries=services'
    + '&autoload=false';
  var s = document.createElement('script');
  s.src = url; s.async = true; s.id = 'kakao-sdk';
  s.onerror = function () { console.error('[Kakao SDK] failed to load:', url); };
  document.head.appendChild(s);
  try {
    var masked = key.slice(0, 4) + '***' + key.slice(-4);
    console.log('[Kakao SDK] injecting sdk.js with key=', masked);
  } catch (_) { }
})();

// 주요 엘리먼트 캐싱
const postForm = document.getElementById('postForm');
const fileInput = document.getElementById('imageFiles');
const previewList = document.getElementById('imagePreviewList');
const thumbnailPreview = document.getElementById('thumbnailPreview');
const thumbnailFileInput = document.getElementById('thumbnailFileInput');

const tbImage = document.getElementById('tb-image');
const tbVideo = document.getElementById('tb-video');
const tbMap = document.getElementById('tb-map');
const tbBold = document.getElementById('tb-bold');
const tbItalic = document.getElementById('tb-italic');
const tbUnderline = document.getElementById('tb-underline');
const tbColor = document.getElementById('tb-color');
const tbAlignLeft = document.getElementById('tb-align-left');
const tbAlignCenter = document.getElementById('tb-align-center');
const tbAlignRight = document.getElementById('tb-align-right');
const tbFontFamily = document.getElementById('tb-fontFamily');
const tbFontSize = document.getElementById('tb-fontSize');
const tbDivider = document.getElementById('tb-divider');
const templateCategory = document.getElementById('templateCategory');
const insertTemplateBtn = document.getElementById('insertTemplateBtn');

const preview = document.getElementById('livePreview');
const textarea = document.getElementById('content');
const cancelBtn = document.getElementById('cancelBtn');

const imageUrlsField = document.getElementById('imageUrls');
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content') || 'X-CSRF-TOKEN';

// 현재 텍스트 스타일 상태
let currentFontFamily = '';
let currentFontSize = '';
let currentColor = '';

// 업로드된 이미지 추적
const gathered = [];           // 본문에서 모은 url들을 input[name=imageUrls]로 보냄
const uploadedImages = new Set(); // 임시 업로드된 URL들
let isCancelling = false;
let cleanupSent = false;

// 초기 설정
preview.setAttribute('contenteditable', 'true');
preview.classList.add('editing');
preview.focus();

// preview에서 입력 시 textarea에 반영
preview.addEventListener('input', () => { textarea.value = preview.innerHTML; });

// === 썸네일 지정 버튼 로직 ===
let thumbnailBtn = null;
preview.addEventListener('mouseover', (e) => {
  if (e.target.tagName === 'IMG') {
    if (thumbnailBtn && thumbnailBtn.parentNode) thumbnailBtn.remove();
    const img = e.target;
    const rect = img.getBoundingClientRect();
    const previewRect = preview.getBoundingClientRect();
    thumbnailBtn = document.createElement('button');
    thumbnailBtn.textContent = '썸네일';
    thumbnailBtn.type = 'button';
    thumbnailBtn.style.cssText = `
      position: absolute;
      left: ${rect.left - previewRect.left + 8}px;
      top: ${rect.top - previewRect.top + 8}px;
      padding: 4px 8px;
      font-size: 11px;
      background: rgba(0, 0, 0, 0.7);
      color: white;
      border: 1px solid rgba(255, 255, 255, 0.3);
      border-radius: 4px;
      cursor: pointer;
      z-index: 1000;
      font-family: 'Noto Sans KR', sans-serif;`;
    thumbnailBtn.addEventListener('click', (evt) => {
      evt.preventDefault(); evt.stopPropagation();
      const thumbnailInput = document.getElementById('thumbnailUrl');
      if (thumbnailInput) {
        thumbnailInput.value = img.src;
        try { if (thumbnailFileInput) thumbnailFileInput.value = ''; } catch (_) { }
        try {
          if (thumbnailPreview) {
            thumbnailPreview.innerHTML = '';
            const wrapper = document.createElement('div'); wrapper.className = 'current-thumbnail';
            const label = document.createElement('span'); label.className = 'thumbnail-preview-label'; label.textContent = '현재 썸네일';
            const thumbImg = document.createElement('img'); thumbImg.className = 'thumbnail-preview-image'; thumbImg.src = img.src; thumbImg.alt = '현재 썸네일';
            wrapper.appendChild(label); wrapper.appendChild(thumbImg); thumbnailPreview.appendChild(wrapper);
          }
        } catch (err) { console.warn('썸네일 미리보기 갱신 실패', err); }
        alert('썸네일로 설정했습니다.');
      }
    });
    preview.style.position = 'relative';
    preview.appendChild(thumbnailBtn);
  }
});
preview.addEventListener('mouseout', (e) => {
  if (e.target.tagName === 'IMG') {
    if (thumbnailBtn && e.relatedTarget === thumbnailBtn) return;
    if (thumbnailBtn && thumbnailBtn.parentNode) { thumbnailBtn.remove(); thumbnailBtn = null; }
  }
});

// === 첨부 파일(이미지) 리스트 미리보기 ===
if (fileInput) {
  fileInput.addEventListener('change', function () {
    const files = Array.from(this.files || []);
    if (!files.length) {
      previewList.innerHTML = '<p style="color:#666;font-size:13px">선택한 파일이 없습니다.</p>';
      return;
    }
    previewList.innerHTML = '';
    const max = 10 * 1024 * 1024; // 10MB per file
    files.forEach((f, idx) => {
      if (f.size > max) { alert(`파일(#${idx + 1}) 크기가 10MB를 초과합니다.`); return; }
      const item = document.createElement('div');
      item.style.cssText = 'padding:8px;border:1px solid #dcdde1;border-radius:6px;background:#f8f9fa;font-size:13px;display:flex;align-items:center;gap:8px';
      const icon = document.createElement('span'); icon.textContent = '[FILE]'; icon.style.fontSize = '12px';
      const name = document.createElement('span'); name.textContent = f.name; name.style.cssText = 'flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap';
      item.appendChild(icon); item.appendChild(name); previewList.appendChild(item);
    });
  });
}

// === 썸네일 파일 선택 시 미리보기 및 URL 초기화 ===
if (thumbnailFileInput) {
  thumbnailFileInput.addEventListener('change', function () {
    const file = this.files && this.files[0];
    if (!file) return;
    if (file.size > 10 * 1024 * 1024) { alert('썸네일 파일 크기가 10MB를 초과합니다.'); this.value = ''; return; }
    const thumbnailInput = document.getElementById('thumbnailUrl'); if (thumbnailInput) thumbnailInput.value = '';
    try {
      if (thumbnailPreview) {
        thumbnailPreview.innerHTML = '';
        const wrapper = document.createElement('div'); wrapper.className = 'current-thumbnail';
        const label = document.createElement('span'); label.className = 'thumbnail-preview-label'; label.textContent = '현재 썸네일';
        const img = document.createElement('img'); img.className = 'thumbnail-preview-image'; img.src = URL.createObjectURL(file); img.alt = '선택한 썸네일';
        wrapper.appendChild(label); wrapper.appendChild(img); thumbnailPreview.appendChild(wrapper);
      }
    } catch (_) { }
  });
}

// === inline 이미지 업로드 ===
document.getElementById('inlineImageUpload')?.addEventListener('change', async (e) => {
  const files = Array.from(e.target.files || []);
  if (!files.length) return;
  const sel = window.getSelection();
  const focusNode = sel?.focusNode;
  const isInPreview = focusNode && (focusNode === preview || preview.contains(focusNode));
  if (!isInPreview) { alert('문단 영역을 활성화한 뒤 이미지를 업로드해주세요.'); e.target.value = ''; return; }
  for (const f of files) {
    if (f.size > 4 * 1024 * 1024) { alert('이미지 크기가 4MB를 초과합니다.'); continue; }
    const fd = new FormData(); fd.append('file', f);
    try {
      const res = await fetch('/api/uploads/image', { method: 'POST', headers: { [csrfHeader]: csrfToken }, body: fd });
      const data = await res.json();
      if (!res.ok) { alert('업로드 실패: ' + (data.error || res.status)); continue; }
      const url = data.url;
      const imgHtml = `<img src="${url}" alt="image" class="resizable-media" style="max-width:100%;cursor:move" draggable="false"><br>`;
      insertHtmlAtCursor(imgHtml);
      gathered.push(url); uploadedImages.add(url); if (imageUrlsField) imageUrlsField.value = gathered.join(',');
    } catch (err) { alert('업로드 처리 중 오류가 발생했습니다.'); }
  }
  e.target.value = '';
});

// === textarea에 HTML 반영 유틸 ===
function insertHtmlAtCursor(html) {
  preview.focus();
  let sel = window.getSelection(); let range;
  if (sel && sel.rangeCount > 0) {
    range = sel.getRangeAt(0);
    const container = range.commonAncestorContainer;
    const isInPreview = preview.contains(container.nodeType === 3 ? container.parentNode : container);
    if (!isInPreview) { range = document.createRange(); range.selectNodeContents(preview); range.collapse(false); sel.removeAllRanges(); sel.addRange(range); }
  } else { range = document.createRange(); range.selectNodeContents(preview); range.collapse(false); sel.removeAllRanges(); sel.addRange(range); }
  if (sel.getRangeAt && sel.rangeCount) {
    range = sel.getRangeAt(0); range.deleteContents();
    const el = document.createElement('div'); el.innerHTML = html; const frag = document.createDocumentFragment();
    let node, lastNode; while ((node = el.firstChild)) { lastNode = frag.appendChild(node); }
    range.insertNode(frag);
    if (lastNode) { range = range.cloneRange(); range.setStartAfter(lastNode); range.collapse(true); sel.removeAllRanges(); sel.addRange(range); }
  }
  textarea.value = preview.innerHTML;
}

// === 텍스트 입력 시 현재 스타일 적용 ===
preview.addEventListener('keypress', (e) => {
  if (currentFontFamily || currentFontSize || currentColor) {
    e.preventDefault();
    const char = e.key; if (char.length === 1) {
      const span = document.createElement('span');
      if (currentFontFamily) span.style.fontFamily = currentFontFamily;
      if (currentFontSize) span.style.fontSize = currentFontSize;
      if (currentColor) span.style.color = currentColor;
      span.textContent = char;
      const sel = window.getSelection(); if (sel.rangeCount > 0) { const range = sel.getRangeAt(0); range.deleteContents(); range.insertNode(span); range.setStartAfter(span); range.collapse(true); sel.removeAllRanges(); sel.addRange(range); }
      textarea.value = preview.innerHTML;
    }
  }
});

// === 선택 텍스트 스타일 래핑 유틸 ===
function wrapSelectionWithTag(tagName, styles = {}) {
  const sel = window.getSelection(); if (!sel.rangeCount) return;
  const range = sel.getRangeAt(0); const selectedText = range.toString(); if (!selectedText) return true;
  const span = document.createElement(tagName); Object.keys(styles).forEach((k) => (span.style[k] = styles[k]));
  try { range.deleteContents(); span.textContent = selectedText; range.insertNode(span); range.setStartAfter(span); range.collapse(true); sel.removeAllRanges(); sel.addRange(range); textarea.value = preview.innerHTML; return false; }
  catch (ex) { console.error('Text format error:', ex); return false; }
}

// Bold / Italic / Underline
if (tbBold) tbBold.addEventListener('click', () => { preview.focus(); document.execCommand('bold', false, null); textarea.value = preview.innerHTML; tbBold.classList.toggle('active'); });
if (tbItalic) tbItalic.addEventListener('click', () => { preview.focus(); document.execCommand('italic', false, null); textarea.value = preview.innerHTML; tbItalic.classList.toggle('active'); });
if (tbUnderline) tbUnderline.addEventListener('click', () => { preview.focus(); document.execCommand('underline', false, null); textarea.value = preview.innerHTML; tbUnderline.classList.toggle('active'); });

// 구분선 삽입
if (tbDivider) tbDivider.addEventListener('click', () => {
  insertHtmlAtCursor('<hr class="post-divider" style="border:none;border-top:1px solid #e6e8eb;margin:16px 0">');
});

// 카테고리 템플릿
const CATEGORY_TEMPLATES = {
  '일상': `
    <h2>오늘의 일상 기록</h2>
    <p>안녕하세요. 오늘 하루를 기록해 봅니다.</p>
    <h3>하이라이트</h3>
    <ul>
      <li>아침: 간단한 운동과 커피</li>
      <li>점심: 동료들과 식사</li>
      <li>저녁: 산책하며 하루 마무리</li>
    </ul>
    <p>작은 루틴들이 쌓여 큰 변화를 만든다고 믿어요 :)</p>
  `,
  '기술': `
    <h2>[기술] 문제 정의와 해결 과정</h2>
    <h3>배경</h3>
    <p>현재 겪고 있는 기술적 이슈를 정리합니다.</p>
    <h3>환경</h3>
    <ul>
      <li>OS/Runtime:</li>
      <li>Framework/Library:</li>
      <li>버전:</li>
    </ul>
    <h3>접근법</h3>
    <ol>
      <li>원인 가설 수립</li>
      <li>재현 및 로그 분석</li>
      <li>해결 및 검증</li>
    </ol>
    <h3>결론</h3>
    <p>핵심 요약과 다음 액션을 남깁니다.</p>
  `,
  '여행': `
    <h2>여행기: 장소/도시 이름</h2>
    <p>여행 일정과 감상을 기록해요.</p>
    <h3>코스</h3>
    <ol>
      <li>1일차: 이동 및 체크인</li>
      <li>2일차: 주요 명소 관람</li>
      <li>3일차: 로컬 맛집 탐방</li>
    </ol>
    <h3>Tip</h3>
    <p>교통/숙소/환전 등 유용한 팁을 정리합니다.</p>
  `,
  '맛집': `
    <h2>맛집 리뷰: 식당 이름</h2>
    <h3>기본 정보</h3>
    <ul>
      <li>위치:</li>
      <li>영업시간:</li>
      <li>대표 메뉴:</li>
    </ul>
    <h3>후기</h3>
    <p>맛/가격/서비스/분위기를 중심으로 솔직 리뷰를 남겨요.</p>
  `,
  '취미': `
    <h2>취미 로그: 주제</h2>
    <h3>시작 동기</h3>
    <p>이 취미를 시작하게 된 계기.</p>
    <h3>진행 상황</h3>
    <ul>
      <li>준비물/자료:</li>
      <li>연습/만들기 기록:</li>
    </ul>
    <h3>느낀 점</h3>
    <p>배운 점과 다음 목표를 적어봅니다.</p>
  `,
};

if (insertTemplateBtn) insertTemplateBtn.addEventListener('click', () => {
  const cat = templateCategory ? templateCategory.value : '';
  if (!cat) { alert('카테고리를 선택하세요.'); return; }
  const html = CATEGORY_TEMPLATES[cat];
  if (!html) { alert('해당 카테고리 템플릿이 없습니다.'); return; }
  const hasContent = (preview.innerHTML || '').trim().length > 0;
  if (!hasContent) {
    preview.innerHTML = html;
  } else {
    if (confirm('현재 내용을 템플릿으로 대체할까요? 취소를 누르면 커서 위치에 삽입합니다.')) {
      preview.innerHTML = html;
    } else {
      insertHtmlAtCursor(html);
    }
  }
  textarea.value = preview.innerHTML;
  preview.focus();
});

// 색상/폰트
if (tbColor) tbColor.addEventListener('change', (e) => { preview.focus(); const val = e.target.value; if (val) { const noSel = wrapSelectionWithTag('span', { color: val }); if (noSel) { currentColor = val; tbColor.style.backgroundColor = '#e3f2fd'; } } else { currentColor = ''; tbColor.style.backgroundColor = '#fff'; } });
if (tbFontFamily) tbFontFamily.addEventListener('change', (e) => { preview.focus(); if (e.target.value) { currentFontFamily = e.target.value; const noSel = wrapSelectionWithTag('span', { fontFamily: e.target.value }); if (noSel) tbFontFamily.style.backgroundColor = '#e3f2fd'; } else { currentFontFamily = ''; tbFontFamily.style.backgroundColor = '#fff'; } });
if (tbFontSize) tbFontSize.addEventListener('change', (e) => { preview.focus(); if (e.target.value) { currentFontSize = e.target.value; const noSel = wrapSelectionWithTag('span', { fontSize: e.target.value }); if (noSel) tbFontSize.style.backgroundColor = '#e3f2fd'; } else { currentFontSize = ''; tbFontSize.style.backgroundColor = '#fff'; } });

// 정렬 적용
function applyAlignment(align) {
  preview.focus(); const sel = window.getSelection(); if (!sel.rangeCount) return; const range = sel.getRangeAt(0);
  try {
    if (range && range.toString()) { const wrapper = document.createElement('div'); wrapper.style.textAlign = align; const contents = range.extractContents(); wrapper.appendChild(contents); range.insertNode(wrapper); sel.removeAllRanges(); const newRange = document.createRange(); newRange.setStartAfter(wrapper); newRange.collapse(true); sel.addRange(newRange); }
    else { insertHtmlAtCursor(`<div style="text-align:${align}"><br></div>`); }
    textarea.value = preview.innerHTML;
  } catch (ex) { console.error('applyAlignment error:', ex); }
}
if (tbAlignLeft) tbAlignLeft.addEventListener('click', () => applyAlignment('left'));
if (tbAlignCenter) tbAlignCenter.addEventListener('click', () => applyAlignment('center'));
if (tbAlignRight) tbAlignRight.addEventListener('click', () => applyAlignment('right'));

// 이미지/동영상 삽입 버튼
if (tbImage) tbImage.addEventListener('click', () => { const inlineInput = document.getElementById('inlineImageUpload'); inlineInput && inlineInput.click(); });
if (tbVideo) {
  tbVideo.addEventListener('click', () => {
    const url = prompt('동영상 URL을 입력해주세요 (YouTube, 직접 업로드 URL)'); if (!url) return;
    let videoHtml = '';
    if (url.includes('youtube.com') || url.includes('youtu.be')) {
      let videoId = '';
      if (url.includes('youtu.be/')) videoId = url.split('youtu.be/')[1].split('?')[0];
      else if (url.includes('youtube.com/watch?v=')) videoId = url.split('v=')[1].split('&')[0];
      if (videoId) {
        videoHtml = `
<div class="resizable-media resizable-video" style="position:relative;width:640px;max-width:100%;margin:16px auto;cursor:nwse-resize;display:block" data-video-id="${videoId}">
  <div style="padding-bottom:56.25%;position:relative">
    <iframe src="https://www.youtube.com/embed/${videoId}" style="position:absolute;top:0;left:0;width:100%;height:100%;border:0;border-radius:8px" allowfullscreen></iframe>
  </div>
</div>`;
      }
    } else {
      videoHtml = `
<div class="resizable-media resizable-video" style="width:640px;max-width:100%;margin:16px auto;cursor:nwse-resize;display:block">
  <video controls style="width:100%;border-radius:8px;display:block">
    <source src="${url}" type="video/mp4">
    브라우저가 동영상을 재생하지 못했습니다.
  </video>
</div>`;
    }
    if (videoHtml) insertHtmlAtCursor(videoHtml); else insertHtmlAtCursor(`<p>동영상 링크: <a href="${url}" target="_blank">${url}</a></p>`);
  });
}

// Kakao 지도 SDK 준비 대기
function ensureKakaoReady(cb) {
  if (window.kakao && kakao.maps && typeof kakao.maps.load === 'function') { kakao.maps.load(cb); return; }
  let waited = 0; const t = setInterval(() => {
    if (window.kakao && kakao.maps && typeof kakao.maps.load === 'function') { clearInterval(t); kakao.maps.load(cb); }
    else if ((waited += 100) > 15000) { clearInterval(t); alert('카카오 지도 SDK 로딩 실패\n- JavaScript 키 확인\n- 사이트 도메인이 Kakao Developers에 등록되었는지 확인\n- 방화벽/네트워크 정책 확인'); }
  }, 100);
}
function renderKakaoMap(containerId, lat, lng) {
  if (!(window.kakao && kakao.maps)) return; const el = document.getElementById(containerId); if (!el) return;
  const map = new kakao.maps.Map(el, { center: new kakao.maps.LatLng(lat, lng), level: 3 });
  const marker = new kakao.maps.Marker({ position: new kakao.maps.LatLng(lat, lng) }); marker.setMap(map);
}
function insertKakaoMapBlock(lat, lng, label) {
  const id = 'kmap-' + Date.now() + '-' + Math.floor(Math.random() * 10000);
  const caption = label ? `<div style=\"font-size:12px;color:#666;margin-top:4px;\">위치 ${label}</div>` : '';
  const html = `
<div class=\"kmap resizable-media\" style=\"margin:8px 0;max-width:100%;width:100%\">
  <div id=\"${id}\" class=\"resizable-map\" style=\"width:100%;height:320px;border-radius:8px;border:1px solid #e6e8eb;cursor:nwse-resize;\"></div>
  ${caption}
</div>
<script>(function(){if(!window.kakao||!kakao.maps){return;}var c=document.getElementById('${id}');if(!c)return;var map=new kakao.maps.Map(c,{center:new kakao.maps.LatLng(${lat},${lng}),level:3});var marker=new kakao.maps.Marker({position:new kakao.maps.LatLng(${lat},${lng})});marker.setMap(map);}());<\/script>
`;
  insertHtmlAtCursor(html); ensureKakaoReady(() => renderKakaoMap(id, lat, lng));
}

// 지도 선택 모달(작성용)
const mapModal = document.getElementById('mapModal');
const mapBackdrop = document.getElementById('mapModalBackdrop');
const mapClose = document.getElementById('mapModalClose');
const mapInsertBtn = document.getElementById('mapInsertBtn');
const mapSearchInput = document.getElementById('mapSearchInput');
const mapSearchBtn = document.getElementById('mapSearchBtn');
const mapPickedInfo = document.getElementById('mapPickedInfo');
let pickerMap, pickerMarker, pickedLatLng, pickedLabel;

function openMapModal() {
  if (!mapModal || !mapBackdrop) return;
  mapBackdrop.style.display = 'block'; mapModal.style.display = 'block';
  ensureKakaoReady(() => {
    if (!pickerMap) {
      const container = document.getElementById('kmap-picker');
      pickerMap = new kakao.maps.Map(container, { center: new kakao.maps.LatLng(37.5665, 126.9780), level: 5 });
      pickerMarker = new kakao.maps.Marker();
      kakao.maps.event.addListener(pickerMap, 'click', function (mouseEvent) {
        const latlng = mouseEvent.latLng; setPicked(latlng.getLat(), latlng.getLng(), null); reverseGeocode(latlng.getLat(), latlng.getLng());
      });
    }
  });
}
function closeMapModal() { if (!mapModal || !mapBackdrop) return; mapBackdrop.style.display = 'none'; mapModal.style.display = 'none'; }
function setPicked(lat, lng, label) {
  pickedLatLng = { lat, lng }; pickedLabel = label || (lat + ', ' + lng);
  if (mapInsertBtn) mapInsertBtn.disabled = false;
  if (mapPickedInfo) mapPickedInfo.textContent = `선택한 위치: ${pickedLabel} (${lat.toFixed(5)}, ${lng.toFixed(5)})`;
  if (pickerMarker) { pickerMarker.setPosition(new kakao.maps.LatLng(lat, lng)); pickerMarker.setMap(pickerMap); }
  pickerMap && pickerMap.panTo(new kakao.maps.LatLng(lat, lng));
}
function reverseGeocode(lat, lng) {
  if (!(kakao.maps.services)) return; const geocoder = new kakao.maps.services.Geocoder();
  geocoder.coord2Address(lng, lat, function (res, status) { if (status === kakao.maps.services.Status.OK && res && res.length) { const addr = res[0].address?.address_name || res[0].road_address?.address_name; if (addr) setPicked(lat, lng, addr); } });
}
function keywordSearch(q) {
  if (!q || !(kakao.maps.services)) return; const places = new kakao.maps.services.Places();
  places.keywordSearch(q, function (data, status) { if (status === kakao.maps.services.Status.OK && data && data.length) { const d = data[0]; const lat = parseFloat(d.y); const lng = parseFloat(d.x); setPicked(lat, lng, d.place_name); } else { alert('검색 결과가 없습니다. 지도를 클릭해 위치를 선택해주세요.'); } });
}
if (tbMap) tbMap.addEventListener('click', openMapModal);
if (mapClose) mapClose.addEventListener('click', closeMapModal);
if (mapBackdrop) mapBackdrop.addEventListener('click', closeMapModal);
if (mapSearchBtn) mapSearchBtn.addEventListener('click', () => keywordSearch(mapSearchInput.value.trim()));
if (mapSearchInput) mapSearchInput.addEventListener('keydown', (e) => { if (e.key === 'Enter') { e.preventDefault(); keywordSearch(mapSearchInput.value.trim()); } });
if (mapInsertBtn) mapInsertBtn.addEventListener('click', () => { if (!pickedLatLng) return; insertKakaoMapBlock(pickedLatLng.lat, pickedLatLng.lng, pickedLabel); closeMapModal(); });

// === 임시 업로드 정리(sendBeacon) ===
function sendCleanup({ keepUsed }) {
  if (cleanupSent) return;
  const params = new URLSearchParams();
  if (keepUsed) {
    const contentHtml = preview.innerHTML;
    const usedImages = Array.from(uploadedImages).filter((url) => contentHtml.includes(url));
    params.append('usedImages', usedImages.join(','));
  } else { params.append('usedImages', ''); params.append('mode', 'cancel'); }
  try { navigator.sendBeacon('/api/uploads/cleanup', params); cleanupSent = true; }
  catch (e) { fetch('/api/uploads/cleanup', { method: 'POST', body: params, keepalive: true }).catch(() => { }); cleanupSent = true; }
}

// 취소 버튼: 임시 업로드 정리 후 /posts로 이동
if (cancelBtn) {
  cancelBtn.addEventListener('click', (e) => {
    if (uploadedImages.size > 0) {
      e.preventDefault(); e.stopPropagation();
      isCancelling = true; sendCleanup({ keepUsed: false });
      const href = cancelBtn.getAttribute('href') || '/posts';
      setTimeout(() => { window.location.href = href; }, 60);
    }
  });
}

// 페이지 이탈 시 임시 업로드 정리
window.addEventListener('beforeunload', () => { if (uploadedImages.size > 0 && !cleanupSent) sendCleanup({ keepUsed: !isCancelling }); });

// 제출 직전 최종 동기화 및 정리 플래그
if (postForm) {
  postForm.addEventListener('submit', () => { textarea.value = preview.innerHTML; isCancelling = false; cleanupSent = true; });
}

// === 미디어 리사이즈 (이미지/비디오/지도 블록 크기 조절) ===
let isResizing = false; let currentElement = null; let startX, startY, startWidth, startHeight; let resizeIndicator = null;
function showResizeIndicator(element, width, height, mouseX, mouseY) {
  if (!resizeIndicator) { resizeIndicator = document.createElement('div'); resizeIndicator.style.cssText = `position:fixed;background:rgba(0,0,0,.8);color:#fff;padding:4px 10px;font-size:12px;border-radius:4px;pointer-events:none;z-index:10000;font-family:'Courier New',monospace;transform:translate(-50%,-130%);`; document.body.appendChild(resizeIndicator); }
  resizeIndicator.textContent = `${Math.round(width)}px × ${Math.round(height)}px`; resizeIndicator.style.left = mouseX + 'px'; resizeIndicator.style.top = mouseY + 'px'; resizeIndicator.style.display = 'block';
}
function hideResizeIndicator() { if (resizeIndicator) resizeIndicator.style.display = 'none'; }

preview.addEventListener('mousedown', (e) => {
  const target = e.target;
  if (target.tagName === 'IMG' && target.classList.contains('resizable-media')) { isResizing = true; currentElement = target; startX = e.clientX; startY = e.clientY; startWidth = target.offsetWidth; e.preventDefault(); }
  if (target.closest('.resizable-video')) { const videoContainer = target.closest('.resizable-video'); isResizing = true; currentElement = videoContainer; startX = e.clientX; startY = e.clientY; startWidth = videoContainer.offsetWidth; e.preventDefault(); }
  if (target.classList.contains('resizable-map') || target.closest('.resizable-map')) { const mapContainer = target.classList.contains('resizable-map') ? target : target.closest('.resizable-map'); isResizing = true; currentElement = mapContainer; startX = e.clientX; startY = e.clientY; startWidth = mapContainer.offsetWidth; startHeight = mapContainer.offsetHeight; e.preventDefault(); }
});

document.addEventListener('mousemove', (e) => {
  if (!isResizing || !currentElement) return; const deltaX = e.clientX - startX;
  if (currentElement.tagName === 'IMG') { const newWidth = Math.max(100, Math.min(startWidth + deltaX, preview.offsetWidth)); currentElement.style.width = newWidth + 'px'; currentElement.style.maxWidth = 'none'; showResizeIndicator(currentElement, newWidth, currentElement.offsetHeight, e.clientX, e.clientY); }
  else if (currentElement.classList.contains('resizable-video')) { const newWidth = Math.max(200, Math.min(startWidth + deltaX, preview.offsetWidth)); const newHeight = (newWidth * 9) / 16; currentElement.style.width = newWidth + 'px'; showResizeIndicator(currentElement, newWidth, newHeight, e.clientX, e.clientY); }
  else if (currentElement.classList.contains('resizable-map')) { const deltaY = e.clientY - startY; const newWidth = Math.max(200, Math.min(startWidth + deltaX, preview.offsetWidth)); const newHeight = Math.max(200, startHeight + deltaY); currentElement.style.width = newWidth + 'px'; currentElement.style.height = newHeight + 'px'; showResizeIndicator(currentElement, newWidth, newHeight, e.clientX, e.clientY); }
  textarea.value = preview.innerHTML; e.preventDefault();
});

document.addEventListener('mouseup', () => { if (isResizing) { isResizing = false; currentElement = null; hideResizeIndicator(); } });
