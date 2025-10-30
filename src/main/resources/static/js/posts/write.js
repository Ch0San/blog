// Kakao Maps SDK 동적 주입
(function () {
    var metaEl = document.querySelector('meta[name="kakao-js-key"]');
    var key = metaEl ? metaEl.getAttribute('content') : '';
    if (!key) {
        console.error('[Kakao SDK] kakaoJsKey is empty. Set property kakao.maps.javascript.key');
        return;
    }
    var url = 'https://dapi.kakao.com/v2/maps/sdk.js?appkey=' + encodeURIComponent(key) + '&libraries=services&autoload=false';
    var s = document.createElement('script'); s.src = url; s.async = true; s.id = 'kakao-sdk';
    s.onerror = function () { console.error('[Kakao SDK] failed to load:', url); };
    document.head.appendChild(s);
    // 진단용: 콘솔에 URL 일부 로그(키 마스킹)
    try {
        var masked = key.slice(0, 4) + '***' + key.slice(-4);
        console.log('[Kakao SDK] injecting sdk.js with key=', masked);
    } catch (e) { }
})();

const fileInput = document.getElementById('imageFiles');
const previewList = document.getElementById('imagePreviewList');
const thumbnailPreview = document.getElementById('thumbnailPreview');
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
const preview = document.getElementById('livePreview');
const textarea = document.getElementById('content');

// 현재 선택된 서식 상태 저장
let currentFontFamily = '';
let currentFontSize = '';
let currentColor = '';

// 미리보기 형식에서 바로 작성 (고정 모드)
preview.setAttribute('contenteditable', 'true');
preview.classList.add('editing');
preview.focus();

// 미리보기에서 타이핑하면 숨은 textarea에 HTML 동기화 (폼 전송용)
preview.addEventListener('input', () => {
    textarea.value = preview.innerHTML;
});

// 에디터 내 이미지에 마우스 오버 시 썸네일 지정 버튼 표시
let thumbnailBtn = null;

preview.addEventListener('mouseover', (e) => {
    if (e.target.tagName === 'IMG') {
        // 기존 버튼 제거
        if (thumbnailBtn && thumbnailBtn.parentNode) {
            thumbnailBtn.remove();
        }

        const img = e.target;
        const rect = img.getBoundingClientRect();
        const previewRect = preview.getBoundingClientRect();

        // 썸네일 버튼 생성
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
            font-family: 'Noto Sans KR', sans-serif;
        `;

        thumbnailBtn.addEventListener('click', (evt) => {
            evt.preventDefault();
            evt.stopPropagation();
            const thumbnailInput = document.getElementById('thumbnailUrl');
            if (thumbnailInput) {
                thumbnailInput.value = img.src;
                // 미리보기 갱신
                try {
                    const previewBox = document.getElementById('thumbnailPreview');
                    if (previewBox) {
                        previewBox.innerHTML = '';
                        const wrapper = document.createElement('div');
                        wrapper.className = 'current-thumbnail';
                        const label = document.createElement('span');
                        label.className = 'thumbnail-preview-label';
                        label.textContent = '현재 썸네일:';
                        const thumbImg = document.createElement('img');
                        thumbImg.className = 'thumbnail-preview-image';
                        thumbImg.src = img.src;
                        thumbImg.alt = '현재 썸네일';
                        wrapper.appendChild(label);
                        wrapper.appendChild(thumbImg);
                        previewBox.appendChild(wrapper);
                    }
                } catch (e) { console.warn('썸네일 미리보기 갱신 실패', e); }
                alert('썸네일 URL로 등록되었습니다: ' + img.src);
            }
        });

        preview.style.position = 'relative';
        preview.appendChild(thumbnailBtn);
    }
});

preview.addEventListener('mouseout', (e) => {
    if (e.target.tagName === 'IMG') {
        // 버튼 영역으로 마우스가 이동한 경우는 유지
        if (thumbnailBtn && e.relatedTarget === thumbnailBtn) {
            return;
        }
        if (thumbnailBtn && thumbnailBtn.parentNode) {
            thumbnailBtn.remove();
            thumbnailBtn = null;
        }
    }
});

if (fileInput) {
    fileInput.addEventListener('change', function () {
        const files = Array.from(this.files || []);
        if (!files.length) {
            previewList.innerHTML = '<p style="color:#666;font-size:13px">선택된 파일이 없습니다.</p>';
            return;
        }

        previewList.innerHTML = '';
        const max = 10 * 1024 * 1024; // 10MB per file
        files.forEach((f, idx) => {
            if (f.size > max) {
                alert(`파일(#${idx + 1}) 크기가 10MB를 초과했습니다.`);
                return;
            }
            const item = document.createElement('div');
            item.style.cssText = 'padding:8px;border:1px solid #dcdde1;border-radius:6px;background:#f8f9fa;font-size:13px;display:flex;align-items:center;gap:8px';

            const icon = document.createElement('span');
            icon.textContent = '📎';
            icon.style.fontSize = '16px';

            const name = document.createElement('span');
            name.textContent = f.name;
            name.style.cssText = 'flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap';

            item.appendChild(icon);
            item.appendChild(name);
            previewList.appendChild(item);
        });
    });
}


const el = {
    title: document.getElementById('title'),
    author: document.getElementById('author'),
    category: document.getElementById('category'),
    tags: document.getElementById('tags'),
    content: document.getElementById('content'),
    thumbUrl: document.getElementById('thumbnailUrl')
};

function nl2br(s) {
    return (s || '').replace(/\n/g, '<br>');
}

function sanitizeHtml(html) {
    if (!html) return '';
    let safe = html.replace(/<\/(?:script|style)>/gi, '')
        .replace(/<(?:script|style)[^>]*>/gi, '');
    safe = safe.replace(/ on[a-zA-Z]+\s*=\s*"[^"]*"/g, '')
        .replace(/ on[a-zA-Z]+\s*=\s*'[^']*'/g, '')
        .replace(/ on[a-zA-Z]+\s*=\s*[^\s>]+/g, '');
    return safe;
}

// contenteditable에 HTML 삽입 (본문 영역에만)
function insertHtmlAtCursor(html) {
    // 에디터 본문에 포커스 설정
    preview.focus();

    let sel, range;
    if (window.getSelection) {
        sel = window.getSelection();

        // 선택 영역이 preview 내부인지 확인
        if (sel.rangeCount > 0) {
            range = sel.getRangeAt(0);
            const container = range.commonAncestorContainer;
            const isInPreview = preview.contains(container.nodeType === 3 ? container.parentNode : container);

            // preview 내부가 아니면 preview 끝에 삽입
            if (!isInPreview) {
                range = document.createRange();
                range.selectNodeContents(preview);
                range.collapse(false); // 끝으로 이동
                sel.removeAllRanges();
                sel.addRange(range);
            }
        } else {
            // 선택 영역이 없으면 preview 끝에 삽입
            range = document.createRange();
            range.selectNodeContents(preview);
            range.collapse(false);
            sel.removeAllRanges();
            sel.addRange(range);
        }

        if (sel.getRangeAt && sel.rangeCount) {
            range = sel.getRangeAt(0);
            range.deleteContents();
            const el = document.createElement('div');
            el.innerHTML = html;
            const frag = document.createDocumentFragment();
            let node, lastNode;
            while ((node = el.firstChild)) {
                lastNode = frag.appendChild(node);
            }
            range.insertNode(frag);
            if (lastNode) {
                range = range.cloneRange();
                range.setStartAfter(lastNode);
                range.collapse(true);
                sel.removeAllRanges();
                sel.addRange(range);
            }
        }
    }
    // textarea에도 동기화
    textarea.value = preview.innerHTML;
}

// 에디터에 입력 시 현재 서식 적용
preview.addEventListener('keypress', (e) => {
    if (currentFontFamily || currentFontSize || currentColor) {
        e.preventDefault();

        const char = e.key;
        if (char.length === 1) { // 일반 문자만
            const span = document.createElement('span');
            if (currentFontFamily) span.style.fontFamily = currentFontFamily;
            if (currentFontSize) span.style.fontSize = currentFontSize;
            if (currentColor) span.style.color = currentColor;
            span.textContent = char;

            const sel = window.getSelection();
            if (sel.rangeCount > 0) {
                const range = sel.getRangeAt(0);
                range.deleteContents();
                range.insertNode(span);
                range.setStartAfter(span);
                range.collapse(true);
                sel.removeAllRanges();
                sel.addRange(range);
            }

            textarea.value = preview.innerHTML;
        }
    }
});

// 텍스트 서식 적용 함수
function wrapSelectionWithTag(tagName, styles = {}) {
    const sel = window.getSelection();
    if (!sel.rangeCount) return;

    const range = sel.getRangeAt(0);
    const selectedText = range.toString();

    if (!selectedText) {
        // 텍스트 선택 없으면 앞으로 입력될 텍스트에 적용
        return true;
    }

    const span = document.createElement(tagName);
    Object.keys(styles).forEach(key => {
        span.style[key] = styles[key];
    });

    try {
        range.deleteContents();
        span.textContent = selectedText;
        range.insertNode(span);

        // 커서를 span 뒤로 이동
        range.setStartAfter(span);
        range.collapse(true);
        sel.removeAllRanges();
        sel.addRange(range);

        textarea.value = preview.innerHTML;
        return false;
    } catch (ex) {
        console.error('Text format error:', ex);
        return false;
    }
}

// 텍스트 서식 툴바 동작
if (tbBold) {
    tbBold.addEventListener('click', () => {
        preview.focus();
        document.execCommand('bold', false, null);
        textarea.value = preview.innerHTML;
        // 활성 상태 토글
        tbBold.classList.toggle('active');
    });
}
if (tbItalic) {
    tbItalic.addEventListener('click', () => {
        preview.focus();
        document.execCommand('italic', false, null);
        textarea.value = preview.innerHTML;
        // 활성 상태 토글
        tbItalic.classList.toggle('active');
    });
}
if (tbUnderline) {
    tbUnderline.addEventListener('click', () => {
        preview.focus();
        document.execCommand('underline', false, null);
        textarea.value = preview.innerHTML;
        // 활성 상태 토글
        tbUnderline.classList.toggle('active');
    });
}
// 글자 색상
if (tbColor) {
    tbColor.addEventListener('change', (e) => {
        preview.focus();
        const val = e.target.value;
        if (val) {
            const noSelection = wrapSelectionWithTag('span', { color: val });
            if (noSelection) {
                currentColor = val;
                tbColor.style.backgroundColor = '#e3f2fd';
            }
        } else {
            currentColor = '';
            tbColor.style.backgroundColor = '#fff';
        }
    });
}
if (tbFontFamily) {
    tbFontFamily.addEventListener('change', (e) => {
        preview.focus();
        if (e.target.value) {
            currentFontFamily = e.target.value;
            const noSelection = wrapSelectionWithTag('span', { fontFamily: e.target.value });
            if (noSelection) {
                // 선택된 텍스트 없으면 서식 적용 표시
                tbFontFamily.style.backgroundColor = '#e3f2fd';
            }
        } else {
            currentFontFamily = '';
            tbFontFamily.style.backgroundColor = '#fff';
        }
    });
}
if (tbFontSize) {
    tbFontSize.addEventListener('change', (e) => {
        preview.focus();
        if (e.target.value) {
            currentFontSize = e.target.value;
            const noSelection = wrapSelectionWithTag('span', { fontSize: e.target.value });
            if (noSelection) {
                // 선택된 텍스트 없으면 서식 적용 표시
                tbFontSize.style.backgroundColor = '#e3f2fd';
            }
        } else {
            currentFontSize = '';
            tbFontSize.style.backgroundColor = '#fff';
        }
    });
}

// 정렬 적용 (선택 영역을 블록으로 감싸거나, 빈 블록 삽입)
function applyAlignment(align) {
    preview.focus();
    const sel = window.getSelection();
    if (!sel.rangeCount) return;
    const range = sel.getRangeAt(0);
    try {
        if (range && range.toString()) {
            const wrapper = document.createElement('div');
            wrapper.style.textAlign = align;
            const contents = range.extractContents();
            wrapper.appendChild(contents);
            range.insertNode(wrapper);
            // 커서를 wrapper 뒤로 이동
            sel.removeAllRanges();
            const newRange = document.createRange();
            newRange.setStartAfter(wrapper);
            newRange.collapse(true);
            sel.addRange(newRange);
        } else {
            // 선택이 없으면 정렬 블록 삽입
            insertHtmlAtCursor(`<div style="text-align:${align}"><br></div>`);
        }
        textarea.value = preview.innerHTML;
    } catch (ex) {
        console.error('applyAlignment error:', ex);
    }
}
if (tbAlignLeft) tbAlignLeft.addEventListener('click', () => applyAlignment('left'));
if (tbAlignCenter) tbAlignCenter.addEventListener('click', () => applyAlignment('center'));
if (tbAlignRight) tbAlignRight.addEventListener('click', () => applyAlignment('right'));

// 툴바 동작
if (tbImage) {
    tbImage.addEventListener('click', () => {
        const inlineInput = document.getElementById('inlineImageUpload');
        inlineInput && inlineInput.click();
    });
}
if (tbVideo) {
    tbVideo.addEventListener('click', () => {
        const url = prompt('동영상 URL을 입력하세요 (YouTube, 직접 업로드 등)');
        if (url) {
            let videoHtml = '';
            // YouTube URL 처리
            if (url.includes('youtube.com') || url.includes('youtu.be')) {
                let videoId = '';
                if (url.includes('youtu.be/')) {
                    videoId = url.split('youtu.be/')[1].split('?')[0];
                } else if (url.includes('youtube.com/watch?v=')) {
                    videoId = url.split('v=')[1].split('&')[0];
                }
                if (videoId) {
                    videoHtml = `<div class="resizable-media resizable-video" style="position:relative;width:640px;max-width:100%;margin:16px auto;cursor:nwse-resize;display:block" data-video-id="${videoId}"><div style="padding-bottom:56.25%;position:relative"><iframe src="https://www.youtube.com/embed/${videoId}" style="position:absolute;top:0;left:0;width:100%;height:100%;border:0;border-radius:8px" allowfullscreen></iframe></div></div>`;
                }
            } else {
                // 일반 비디오 URL (mp4, webm 등)
                videoHtml = `<div class="resizable-media resizable-video" style="width:640px;max-width:100%;margin:16px auto;cursor:nwse-resize;display:block"><video controls style="width:100%;border-radius:8px;display:block"><source src="${url}" type="video/mp4">브라우저가 비디오를 지원하지 않습니다.</video></div>`;
            }
            if (videoHtml) {
                insertHtmlAtCursor(videoHtml);
            } else {
                insertHtmlAtCursor(`<p>동영상: <a href="${url}" target="_blank">${url}</a></p>`);
            }
        }
    });
}

// Kakao 지도: SDK 준비 대기
function ensureKakaoReady(cb) {
    // SDK가 로드되었고 autoload=false 인 경우 kakao.maps.load를 통해 초기화
    if (window.kakao && kakao.maps && typeof kakao.maps.load === 'function') {
        kakao.maps.load(cb);
        return;
    }
    let waited = 0;
    const t = setInterval(() => {
        if (window.kakao && kakao.maps && typeof kakao.maps.load === 'function') {
            clearInterval(t);
            kakao.maps.load(cb);
        } else if ((waited += 100) > 15000) {
            clearInterval(t);
            alert('카카오 지도 SDK 로딩 실패\n- JavaScript 키가 맞는지\n- Kakao Developers에 현재 도메인이 등록되었는지\n- 광고차단 확장 프로그램이 차단하지 않는지 확인하세요.');
        }
    }, 100);
}

function renderKakaoMap(containerId, lat, lng) {
    if (!(window.kakao && kakao.maps)) return;
    const el = document.getElementById(containerId);
    if (!el) return;
    const map = new kakao.maps.Map(el, { center: new kakao.maps.LatLng(lat, lng), level: 3 });
    const marker = new kakao.maps.Marker({ position: new kakao.maps.LatLng(lat, lng) });
    marker.setMap(map);
}

function insertKakaoMapBlock(lat, lng, label) {
    const id = 'kmap-' + Date.now() + '-' + Math.floor(Math.random() * 10000);
    const caption = label ? `<div style="font-size:12px;color:#666;margin-top:4px;">📍 ${label}</div>` : '';
    const html = `\n<div class="kmap resizable-media" style="margin:8px 0;max-width:100%;width:100%">\n  <div id="${id}" class="resizable-map" style="width:100%;height:320px;border-radius:8px;border:1px solid #e6e8eb;cursor:nwse-resize;"></div>\n  ${caption}\n</div>\n<script>(function(){if(!window.kakao||!kakao.maps){return;}var c=document.getElementById('${id}');if(!c)return;var map=new kakao.maps.Map(c,{center:new kakao.maps.LatLng(${lat},${lng}),level:3});var marker=new kakao.maps.Marker({position:new kakao.maps.LatLng(${lat},${lng})});marker.setMap(map);}());<\/script>\n`;
    insertHtmlAtCursor(html);
    ensureKakaoReady(() => renderKakaoMap(id, lat, lng));
}

// 지도 선택 모달 로직
const mapModal = document.getElementById('mapModal');
const mapBackdrop = document.getElementById('mapModalBackdrop');
const mapClose = document.getElementById('mapModalClose');
const mapInsertBtn = document.getElementById('mapInsertBtn');
const mapSearchInput = document.getElementById('mapSearchInput');
const mapSearchBtn = document.getElementById('mapSearchBtn');
const mapPickedInfo = document.getElementById('mapPickedInfo');
let pickerMap, pickerMarker, pickedLatLng, pickedLabel;

function openMapModal() {
    mapBackdrop.style.display = 'block';
    mapModal.style.display = 'block';
    ensureKakaoReady(() => {
        if (!pickerMap) {
            const container = document.getElementById('kmap-picker');
            pickerMap = new kakao.maps.Map(container, { center: new kakao.maps.LatLng(37.5665, 126.9780), level: 5 }); // 서울 시청 근방
            pickerMarker = new kakao.maps.Marker();
            kakao.maps.event.addListener(pickerMap, 'click', function (mouseEvent) {
                const latlng = mouseEvent.latLng;
                setPicked(latlng.getLat(), latlng.getLng(), null);
                reverseGeocode(latlng.getLat(), latlng.getLng());
            });
        }
    });
}
function closeMapModal() {
    mapBackdrop.style.display = 'none';
    mapModal.style.display = 'none';
}
function setPicked(lat, lng, label) {
    pickedLatLng = { lat, lng };
    pickedLabel = label || (lat + ', ' + lng);
    mapInsertBtn.disabled = false;
    mapPickedInfo.textContent = `선택된 위치: ${pickedLabel} (${lat.toFixed(5)}, ${lng.toFixed(5)})`;
    if (pickerMarker) {
        pickerMarker.setPosition(new kakao.maps.LatLng(lat, lng));
        pickerMarker.setMap(pickerMap);
    }
    pickerMap && pickerMap.panTo(new kakao.maps.LatLng(lat, lng));
}
function reverseGeocode(lat, lng) {
    if (!(kakao.maps.services)) return;
    const geocoder = new kakao.maps.services.Geocoder();
    geocoder.coord2Address(lng, lat, function (res, status) {
        if (status === kakao.maps.services.Status.OK && res && res.length) {
            const addr = res[0].address?.address_name || res[0].road_address?.address_name;
            if (addr) setPicked(lat, lng, addr);
        }
    });
}
function keywordSearch(q) {
    if (!q || !(kakao.maps.services)) return;
    const places = new kakao.maps.services.Places();
    places.keywordSearch(q, function (data, status) {
        if (status === kakao.maps.services.Status.OK && data && data.length) {
            const d = data[0];
            const lat = parseFloat(d.y), lng = parseFloat(d.x);
            setPicked(lat, lng, d.place_name);
        } else {
            alert('검색 결과가 없습니다. 지도를 클릭해 직접 선택하세요.');
        }
    });
}

if (tbMap) {
    tbMap.addEventListener('click', openMapModal);
}
mapClose.addEventListener('click', closeMapModal);
mapBackdrop.addEventListener('click', closeMapModal);
mapSearchBtn.addEventListener('click', () => keywordSearch(mapSearchInput.value.trim()));
mapSearchInput.addEventListener('keydown', (e) => { if (e.key === 'Enter') { e.preventDefault(); keywordSearch(mapSearchInput.value.trim()); } });
mapInsertBtn.addEventListener('click', () => {
    if (!pickedLatLng) return;
    insertKakaoMapBlock(pickedLatLng.lat, pickedLatLng.lng, pickedLabel);
    closeMapModal();
});

// AJAX 업로드 설정
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content') || 'X-CSRF-TOKEN';
const imageUrlsField = document.getElementById('imageUrls');
const gathered = [];

// 업로드된 이미지 추적
const uploadedImages = new Set();
let isCancelling = false;
let cleanupSent = false;

function sendCleanup({ keepUsed }) {
    if (cleanupSent) return;
    const params = new URLSearchParams();
    if (keepUsed) {
        // 본문에 포함된 이미지는 유지(서버에서 세션 임시 파일 중 나머지를 정리)
        const contentHtml = preview.innerHTML;
        const usedImages = Array.from(uploadedImages).filter(url => contentHtml.includes(url));
        params.append('usedImages', usedImages.join(','));
    } else {
        // 취소 시: 모두 삭제 (서버가 세션 임시 파일 전체 정리)
        params.append('usedImages', '');
        params.append('mode', 'cancel');
    }
    try {
        navigator.sendBeacon('/api/uploads/cleanup', params);
        cleanupSent = true;
    } catch (e) {
        // sendBeacon 실패 시 fetch로 폴백 (비차단 best-effort)
        fetch('/api/uploads/cleanup', { method: 'POST', body: params, keepalive: true }).catch(() => { });
        cleanupSent = true;
    }
}

document.getElementById('inlineImageUpload').addEventListener('change', async (e) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    // 본문 영역에 포커스가 없으면 경고
    const sel = window.getSelection();
    const focusNode = sel.focusNode;
    const isInPreview = focusNode && (focusNode === preview || preview.contains(focusNode));

    if (!isInPreview) {
        alert('본문 영역을 클릭한 후 이미지를 삽입해주세요.');
        e.target.value = '';
        return;
    }

    for (const f of files) {
        if (f.size > 4 * 1024 * 1024) {
            alert('이미지 크기가 4MB를 초과했습니다.');
            continue;
        }
        const fd = new FormData();
        fd.append('file', f);
        try {
            const res = await fetch('/api/uploads/image', {
                method: 'POST',
                headers: { [csrfHeader]: csrfToken },
                body: fd
            });
            const data = await res.json();
            if (!res.ok) {
                alert('업로드 실패: ' + (data.error || res.status));
                continue;
            }
            const url = data.url;
            const imgHtml = `<img src="${url}" alt="image" class="resizable-media" style="max-width:100%;cursor:move" draggable="false"><br>`;
            insertHtmlAtCursor(imgHtml);
            gathered.push(url);
            uploadedImages.add(url); // 추적 목록에 추가
            imageUrlsField.value = gathered.join(',');
        } catch (err) {
            alert('업로드 중 오류가 발생했습니다.');
        }
    }
    e.target.value = '';
});

// 페이지 이탈 시 사용되지 않은 임시 이미지 정리
window.addEventListener('beforeunload', () => {
    if (uploadedImages.size > 0 && !cleanupSent) {
        // 취소가 아닌 일반 이탈: 본문에 사용된 이미지는 유지, 나머지만 서버에서 정리
        sendCleanup({ keepUsed: !isCancelling });
    }
});

// 취소 버튼 클릭 시: 업로드했던 임시 파일 전부 삭제 후 이동
document.addEventListener('DOMContentLoaded', () => {
    const cancelBtn = document.querySelector('.form-actions .btn.btn-secondary');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', (e) => {
            if (uploadedImages.size > 0) {
                e.preventDefault();
                e.stopPropagation();
                isCancelling = true;
                sendCleanup({ keepUsed: false });
                // 비콘 전송 시간을 조금 확보한 뒤 이동
                const href = cancelBtn.getAttribute('href') || '/posts';
                setTimeout(() => { window.location.href = href; }, 60);
            }
        });
    }
});

// 미디어 리사이징 기능
let isResizing = false;
let currentElement = null;
let startX, startY, startWidth, startHeight;
let resizeIndicator = null;

// 크기 표시 인디케이터 생성
function showResizeIndicator(element, width, height, mouseX, mouseY) {
    if (!resizeIndicator) {
        resizeIndicator = document.createElement('div');
        resizeIndicator.style.cssText = `
            position: fixed;
            background: rgba(0, 0, 0, 0.8);
            color: white;
            padding: 4px 10px;
            font-size: 12px;
            border-radius: 4px;
            pointer-events: none;
            z-index: 10000;
            font-family: 'Courier New', monospace;
            transform: translate(-50%, -130%);
        `;
        document.body.appendChild(resizeIndicator);
    }

    resizeIndicator.textContent = `${Math.round(width)}px × ${Math.round(height)}px`;
    resizeIndicator.style.left = mouseX + 'px';
    resizeIndicator.style.top = mouseY + 'px';
    resizeIndicator.style.display = 'block';
}

function hideResizeIndicator() {
    if (resizeIndicator) {
        resizeIndicator.style.display = 'none';
    }
}

preview.addEventListener('mousedown', (e) => {
    const target = e.target;

    // 이미지 리사이징
    if (target.tagName === 'IMG' && target.classList.contains('resizable-media')) {
        isResizing = true;
        currentElement = target;
        startX = e.clientX;
        startY = e.clientY;
        startWidth = target.offsetWidth;
        e.preventDefault();
    }

    // 비디오 컨테이너 리사이징 (YouTube iframe wrapper 또는 video wrapper)
    if (target.closest('.resizable-video')) {
        const videoContainer = target.closest('.resizable-video');
        isResizing = true;
        currentElement = videoContainer;
        startX = e.clientX;
        startY = e.clientY;
        startWidth = videoContainer.offsetWidth;
        e.preventDefault();
    }

    // 지도 리사이징
    if (target.classList.contains('resizable-map') || target.closest('.resizable-map')) {
        const mapContainer = target.classList.contains('resizable-map') ? target : target.closest('.resizable-map');
        isResizing = true;
        currentElement = mapContainer;
        startX = e.clientX;
        startY = e.clientY;
        startWidth = mapContainer.offsetWidth;
        startHeight = mapContainer.offsetHeight;
        e.preventDefault();
    }
});

document.addEventListener('mousemove', (e) => {
    if (!isResizing || !currentElement) return;

    const deltaX = e.clientX - startX;

    if (currentElement.tagName === 'IMG') {
        // 이미지 크기 조절
        const newWidth = Math.max(100, Math.min(startWidth + deltaX, preview.offsetWidth));
        currentElement.style.width = newWidth + 'px';
        currentElement.style.maxWidth = 'none';
        showResizeIndicator(currentElement, newWidth, currentElement.offsetHeight, e.clientX, e.clientY);
    } else if (currentElement.classList.contains('resizable-video')) {
        // 비디오 크기 조절
        const newWidth = Math.max(200, Math.min(startWidth + deltaX, preview.offsetWidth));
        const newHeight = newWidth * 9 / 16;
        currentElement.style.width = newWidth + 'px';
        showResizeIndicator(currentElement, newWidth, newHeight, e.clientX, e.clientY);
    } else if (currentElement.classList.contains('resizable-map')) {
        // 지도 크기 조절 (너비와 높이)
        const deltaY = e.clientY - startY;
        const newWidth = Math.max(200, Math.min(startWidth + deltaX, preview.offsetWidth));
        const newHeight = Math.max(200, startHeight + deltaY);
        currentElement.style.width = newWidth + 'px';
        currentElement.style.height = newHeight + 'px';
        showResizeIndicator(currentElement, newWidth, newHeight, e.clientX, e.clientY);
    }

    textarea.value = preview.innerHTML;
    e.preventDefault();
});

document.addEventListener('mouseup', () => {
    if (isResizing) {
        isResizing = false;
        currentElement = null;
        hideResizeIndicator();
    }
});
