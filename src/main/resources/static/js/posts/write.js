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
    s.src = url;
    s.async = true;
    s.id = 'kakao-sdk';
    s.onerror = function () {
        console.error('[Kakao SDK] failed to load:', url);
    };
    document.head.appendChild(s);

    // 진단 로그 (키 마스킹)
    try {
        var masked = key.slice(0, 4) + '***' + key.slice(-4);
        console.log('[Kakao SDK] injecting sdk.js with key=', masked);
    } catch (_) {}
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

const preview = document.getElementById('livePreview');
const textarea = document.getElementById('content');
const cancelBtn = document.getElementById('cancelBtn');

const imageUrlsField = document.getElementById('imageUrls');
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content') || 'X-CSRF-TOKEN';

// 에디터 상태
let currentFontFamily = '';
let currentFontSize = '';
let currentColor = '';

// 업로드된 이미지들 추적
const gathered = [];           // 서버에서 돌려준 url들 모아 input[name=imageUrls]로 보낼 리스트
const uploadedImages = new Set(); // 임시 업로드된 URL들
let isCancelling = false;
let cleanupSent = false;

// 초기 세팅
preview.setAttribute('contenteditable', 'true');
preview.classList.add('editing');
preview.focus();

// 에디터에서 내용 바뀔 때마다 textarea에 동기화
preview.addEventListener('input', () => {
    textarea.value = preview.innerHTML;
});

// === 썸네일 지정 버튼 로직 ===
let thumbnailBtn = null;
preview.addEventListener('mouseover', (e) => {
    if (e.target.tagName === 'IMG') {
        if (thumbnailBtn && thumbnailBtn.parentNode) {
            thumbnailBtn.remove();
        }

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
            font-family: 'Noto Sans KR', sans-serif;
        `;

        thumbnailBtn.addEventListener('click', (evt) => {
            evt.preventDefault();
            evt.stopPropagation();
            const thumbnailInput = document.getElementById('thumbnailUrl');
            if (thumbnailInput) {
                thumbnailInput.value = img.src;

                // 파일로 선택 중이었다면 파일 선택 해제
                try {
                    if (thumbnailFileInput) thumbnailFileInput.value = '';
                } catch (_) {}

                // 썸네일 미리보기 갱신
                try {
                    if (thumbnailPreview) {
                        thumbnailPreview.innerHTML = '';
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
                        thumbnailPreview.appendChild(wrapper);
                    }
                } catch (err) {
                    console.warn('썸네일 미리보기 갱신 실패', err);
                }

                alert('썸네일로 등록되었습니다.');
            }
        });

        preview.style.position = 'relative';
        preview.appendChild(thumbnailBtn);
    }
});

preview.addEventListener('mouseout', (e) => {
    if (e.target.tagName === 'IMG') {
        if (thumbnailBtn && e.relatedTarget === thumbnailBtn) {
            return; // 버튼 위로 마우스 올라갔으면 유지
        }
        if (thumbnailBtn && thumbnailBtn.parentNode) {
            thumbnailBtn.remove();
            thumbnailBtn = null;
        }
    }
});

// === 첨부 파일(사이드바) 미리보기 ===
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
            item.style.cssText =
                'padding:8px;border:1px solid #dcdde1;border-radius:6px;background:#f8f9fa;font-size:13px;display:flex;align-items:center;gap:8px';

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

// 썸네일 파일 선택 시 미리보기 및 URL 해제
if (thumbnailFileInput) {
    thumbnailFileInput.addEventListener('change', function () {
        const file = this.files && this.files[0];
        if (!file) return;

        // 10MB 제한
        if (file.size > 10 * 1024 * 1024) {
            alert('이미지 파일 크기가 10MB를 초과합니다.');
            this.value = '';
            return;
        }

        // URL 기반 썸네일 제거
        const thumbnailInput = document.getElementById('thumbnailUrl');
        if (thumbnailInput) thumbnailInput.value = '';

        // 미리보기 갱신
        try {
            if (thumbnailPreview) {
                thumbnailPreview.innerHTML = '';
                const wrapper = document.createElement('div');
                wrapper.className = 'current-thumbnail';

                const label = document.createElement('span');
                label.className = 'thumbnail-preview-label';
                label.textContent = '선택한 썸네일:';

                const img = document.createElement('img');
                img.className = 'thumbnail-preview-image';
                img.src = URL.createObjectURL(file);
                img.alt = '선택한 썸네일';

                wrapper.appendChild(label);
                wrapper.appendChild(img);
                thumbnailPreview.appendChild(wrapper);
            }
        } catch (_) {}
    });
}

// === inline(본문) 이미지 업로드 ===
document.getElementById('inlineImageUpload').addEventListener('change', async (e) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    // 본문 포커스 확인
    const sel = window.getSelection();
    const focusNode = sel.focusNode;
    const isInPreview =
        focusNode &&
        (focusNode === preview || preview.contains(focusNode));

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
            const imgHtml =
                `<img src="${url}" alt="image" class="resizable-media" style="max-width:100%;cursor:move" draggable="false"><br>`;

            insertHtmlAtCursor(imgHtml);

            // 서버에서 받은 이미지 URL 추적
            gathered.push(url);
            uploadedImages.add(url);
            imageUrlsField.value = gathered.join(',');

        } catch (err) {
            alert('업로드 중 오류가 발생했습니다.');
        }
    }

    e.target.value = '';
});

// === textarea에 HTML 삽입 도우미 ===
function insertHtmlAtCursor(html) {
    preview.focus();

    let sel = window.getSelection();
    let range;

    // 선택 영역이 preview 바깥이면 preview 끝으로 옮겨
    if (sel && sel.rangeCount > 0) {
        range = sel.getRangeAt(0);
        const container = range.commonAncestorContainer;
        const isInPreview = preview.contains(
            container.nodeType === 3 ? container.parentNode : container
        );

        if (!isInPreview) {
            range = document.createRange();
            range.selectNodeContents(preview);
            range.collapse(false);
            sel.removeAllRanges();
            sel.addRange(range);
        }
    } else {
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

    textarea.value = preview.innerHTML;
}

// === 텍스트 스타일링 관련 ===
preview.addEventListener('keypress', (e) => {
    if (currentFontFamily || currentFontSize || currentColor) {
        e.preventDefault();

        const char = e.key;
        if (char.length === 1) {
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

function wrapSelectionWithTag(tagName, styles = {}) {
    const sel = window.getSelection();
    if (!sel.rangeCount) return;

    const range = sel.getRangeAt(0);
    const selectedText = range.toString();
    if (!selectedText) {
        return true; // 아무 것도 선택 안 한 경우 -> 이후 타이핑 기본 스타일만 바꿔둠
    }

    const span = document.createElement(tagName);
    Object.keys(styles).forEach(key => {
        span.style[key] = styles[key];
    });

    try {
        range.deleteContents();
        span.textContent = selectedText;
        range.insertNode(span);

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

// Bold / Italic / Underline
if (tbBold) {
    tbBold.addEventListener('click', () => {
        preview.focus();
        document.execCommand('bold', false, null);
        textarea.value = preview.innerHTML;
        tbBold.classList.toggle('active');
    });
}
if (tbItalic) {
    tbItalic.addEventListener('click', () => {
        preview.focus();
        document.execCommand('italic', false, null);
        textarea.value = preview.innerHTML;
        tbItalic.classList.toggle('active');
    });
}
if (tbUnderline) {
    tbUnderline.addEventListener('click', () => {
        preview.focus();
        document.execCommand('underline', false, null);
        textarea.value = preview.innerHTML;
        tbUnderline.classList.toggle('active');
    });
}

// 글자색
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

// 글꼴
if (tbFontFamily) {
    tbFontFamily.addEventListener('change', (e) => {
        preview.focus();
        if (e.target.value) {
            currentFontFamily = e.target.value;
            const noSelection = wrapSelectionWithTag('span', { fontFamily: e.target.value });
            if (noSelection) {
                tbFontFamily.style.backgroundColor = '#e3f2fd';
            }
        } else {
            currentFontFamily = '';
            tbFontFamily.style.backgroundColor = '#fff';
        }
    });
}

// 글자 크기
if (tbFontSize) {
    tbFontSize.addEventListener('change', (e) => {
        preview.focus();
        if (e.target.value) {
            currentFontSize = e.target.value;
            const noSelection = wrapSelectionWithTag('span', { fontSize: e.target.value });
            if (noSelection) {
                tbFontSize.style.backgroundColor = '#e3f2fd';
            }
        } else {
            currentFontSize = '';
            tbFontSize.style.backgroundColor = '#fff';
        }
    });
}

// 정렬
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

            sel.removeAllRanges();
            const newRange = document.createRange();
            newRange.setStartAfter(wrapper);
            newRange.collapse(true);
            sel.addRange(newRange);
        } else {
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

// 이미지 삽입 버튼
if (tbImage) {
    tbImage.addEventListener('click', () => {
        const inlineInput = document.getElementById('inlineImageUpload');
        inlineInput && inlineInput.click();
    });
}

// 비디오 삽입 버튼
if (tbVideo) {
    tbVideo.addEventListener('click', () => {
        const url = prompt('동영상 URL을 입력하세요 (YouTube, 직접 업로드 등)');
        if (!url) return;

        let videoHtml = '';

        // YouTube 추출
        if (url.includes('youtube.com') || url.includes('youtu.be')) {
            let videoId = '';
            if (url.includes('youtu.be/')) {
                videoId = url.split('youtu.be/')[1].split('?')[0];
            } else if (url.includes('youtube.com/watch?v=')) {
                videoId = url.split('v=')[1].split('&')[0];
            }
            if (videoId) {
                videoHtml = `
<div class="resizable-media resizable-video"
     style="position:relative;width:640px;max-width:100%;margin:16px auto;cursor:nwse-resize;display:block"
     data-video-id="${videoId}">
  <div style="padding-bottom:56.25%;position:relative">
    <iframe
        src="https://www.youtube.com/embed/${videoId}"
        style="position:absolute;top:0;left:0;width:100%;height:100%;border:0;border-radius:8px"
        allowfullscreen
    ></iframe>
  </div>
</div>`;
            }
        } else {
            // 일반 비디오 URL
            videoHtml = `
<div class="resizable-media resizable-video"
     style="width:640px;max-width:100%;margin:16px auto;cursor:nwse-resize;display:block">
  <video controls style="width:100%;border-radius:8px;display:block">
    <source src="${url}" type="video/mp4">
    브라우저가 비디오를 지원하지 않습니다.
  </video>
</div>`;
        }

        if (videoHtml) {
            insertHtmlAtCursor(videoHtml);
        } else {
            insertHtmlAtCursor(
                `<p>동영상: <a href="${url}" target="_blank">${url}</a></p>`
            );
        }
    });
}

// 지도 버튼
if (tbMap) {
    tbMap.addEventListener('click', openMapModal);
}

// Kakao 지도 SDK 초기화 도우미
function ensureKakaoReady(cb) {
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
            alert(
                '카카오 지도 SDK 로딩 실패\n' +
                '- JavaScript 키 확인\n' +
                '- 현 도메인이 Kakao Developers에 등록됐는지 확인\n' +
                '- 광고차단 플러그인 차단 여부 확인'
            );
        }
    }, 100);
}

function renderKakaoMap(containerId, lat, lng) {
    if (!(window.kakao && kakao.maps)) return;
    const el = document.getElementById(containerId);
    if (!el) return;

    const map = new kakao.maps.Map(el, {
        center: new kakao.maps.LatLng(lat, lng),
        level: 3
    });
    const marker = new kakao.maps.Marker({
        position: new kakao.maps.LatLng(lat, lng)
    });
    marker.setMap(map);
}

// 본문에 지도 블럭 삽입
function insertKakaoMapBlock(lat, lng, label) {
    const id = 'kmap-' + Date.now() + '-' + Math.floor(Math.random() * 10000);
    const caption = label
        ? `<div style="font-size:12px;color:#666;margin-top:4px;">📍 ${label}</div>`
        : '';
    const html = `
<div class="kmap resizable-media" style="margin:8px 0;max-width:100%;width:100%">
  <div
    id="${id}"
    class="resizable-map"
    style="width:100%;height:320px;border-radius:8px;border:1px solid #e6e8eb;cursor:nwse-resize;"
  ></div>
  ${caption}
</div>
<script>(function(){
if(!window.kakao||!kakao.maps){return;}
var c=document.getElementById('${id}');
if(!c)return;
var map=new kakao.maps.Map(c,{
  center:new kakao.maps.LatLng(${lat},${lng}),
  level:3
});
var marker=new kakao.maps.Marker({
  position:new kakao.maps.LatLng(${lat},${lng})
});
marker.setMap(map);
}());<\/script>
`;

    insertHtmlAtCursor(html);

    ensureKakaoReady(() => renderKakaoMap(id, lat, lng));
}

// 지도 모달 관련 DOM
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
            pickerMap = new kakao.maps.Map(container, {
                center: new kakao.maps.LatLng(37.5665, 126.9780), // 서울 시청 근처
                level: 5
            });
            pickerMarker = new kakao.maps.Marker();

            kakao.maps.event.addListener(
                pickerMap,
                'click',
                function (mouseEvent) {
                    const latlng = mouseEvent.latLng;
                    setPicked(latlng.getLat(), latlng.getLng(), null);
                    reverseGeocode(latlng.getLat(), latlng.getLng());
                }
            );
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
    mapPickedInfo.textContent =
        `선택된 위치: ${pickedLabel} (${lat.toFixed(5)}, ${lng.toFixed(5)})`;

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
            const addr = res[0].address?.address_name
                      || res[0].road_address?.address_name;
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
            const lat = parseFloat(d.y);
            const lng = parseFloat(d.x);
            setPicked(lat, lng, d.place_name);
        } else {
            alert('검색 결과가 없습니다. 지도를 클릭해 직접 선택하세요.');
        }
    });
}

mapClose.addEventListener('click', closeMapModal);
mapBackdrop.addEventListener('click', closeMapModal);

mapSearchBtn.addEventListener('click', () => {
    keywordSearch(mapSearchInput.value.trim());
});

mapSearchInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
        e.preventDefault();
        keywordSearch(mapSearchInput.value.trim());
    }
});

mapInsertBtn.addEventListener('click', () => {
    if (!pickedLatLng) return;
    insertKakaoMapBlock(pickedLatLng.lat, pickedLatLng.lng, pickedLabel);
    closeMapModal();
});

// === 임시 업로드된 이미지 정리(sendBeacon) 관련 ===
function sendCleanup({ keepUsed }) {
    if (cleanupSent) return;

    const params = new URLSearchParams();
    if (keepUsed) {
        // 본문(html)에 실제로 사용된 이미지만 남기고 나머지는 삭제하라고 알림
        const contentHtml = preview.innerHTML;
        const usedImages = Array.from(uploadedImages)
            .filter(url => contentHtml.includes(url));
        params.append('usedImages', usedImages.join(','));
    } else {
        // 취소하면 전부 삭제
        params.append('usedImages', '');
        params.append('mode', 'cancel');
    }

    try {
        navigator.sendBeacon('/api/uploads/cleanup', params);
        cleanupSent = true;
    } catch (e) {
        // Beacon이 안 될 수도 있으니까 fetch fallback
        fetch('/api/uploads/cleanup', {
            method: 'POST',
            body: params,
            keepalive: true
        }).catch(() => {});
        cleanupSent = true;
    }
}

// 취소 버튼 눌렀을 때: 임시 업로드 전부 버리고 /posts 로 나감
if (cancelBtn) {
    cancelBtn.addEventListener('click', (e) => {
        if (uploadedImages.size > 0) {
            e.preventDefault();
            e.stopPropagation();

            isCancelling = true;
            sendCleanup({ keepUsed: false });

            const href = cancelBtn.getAttribute('href') || '/posts';
            setTimeout(() => {
                window.location.href = href;
            }, 60);
        }
    });
}

// 페이지 이탈 시: 사용 안 된 임시 이미지는 정리
window.addEventListener('beforeunload', () => {
    if (uploadedImages.size > 0 && !cleanupSent) {
        // 정상 제출(submit)로 종료되는 경우엔 cleanupSent를 true로 만들거라서 여기 안 탐
        sendCleanup({ keepUsed: !isCancelling });
    }
});

// === 폼 제출 직전 최종 동기화 ===
// 이게 중요함: preview 내용을 textarea로 복사하고
// cleanupSent=true로 바꿔서 서버가 임시파일을 지우지 않게 보호
if (postForm) {
    postForm.addEventListener('submit', () => {
        // 본문 최종 HTML -> textarea로
        textarea.value = preview.innerHTML;

        // "나는 정상 저장 중이다" 표시
        isCancelling = false;
        cleanupSent = true;
    });
}

// === 리사이징 로직 (이미지/비디오/지도 박스 드래그로 크기 조절) ===
let isResizing = false;
let currentElement = null;
let startX, startY, startWidth, startHeight;
let resizeIndicator = null;

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

    // 이미지 크기 조절
    if (target.tagName === 'IMG' && target.classList.contains('resizable-media')) {
        isResizing = true;
        currentElement = target;
        startX = e.clientX;
        startY = e.clientY;
        startWidth = target.offsetWidth;
        e.preventDefault();
    }

    // 비디오 컨테이너(iframe wrapper나 video wrapper)
    if (target.closest('.resizable-video')) {
        const videoContainer = target.closest('.resizable-video');
        isResizing = true;
        currentElement = videoContainer;
        startX = e.clientX;
        startY = e.clientY;
        startWidth = videoContainer.offsetWidth;
        e.preventDefault();
    }

    // 지도 컨테이너
    if (target.classList.contains('resizable-map') || target.closest('.resizable-map')) {
        const mapContainer = target.classList.contains('resizable-map')
            ? target
            : target.closest('.resizable-map');

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
        // 이미지
        const newWidth = Math.max(100, Math.min(startWidth + deltaX, preview.offsetWidth));
        currentElement.style.width = newWidth + 'px';
        currentElement.style.maxWidth = 'none';

        showResizeIndicator(currentElement, newWidth, currentElement.offsetHeight, e.clientX, e.clientY);
    } else if (currentElement.classList.contains('resizable-video')) {
        // 비디오 (16:9 가정)
        const newWidth = Math.max(200, Math.min(startWidth + deltaX, preview.offsetWidth));
        const newHeight = newWidth * 9 / 16;
        currentElement.style.width = newWidth + 'px';

        showResizeIndicator(currentElement, newWidth, newHeight, e.clientX, e.clientY);
    } else if (currentElement.classList.contains('resizable-map')) {
        // 지도 박스 (width/height 둘 다)
        const deltaY = e.clientY - startY;
        const newWidth = Math.max(200, Math.min(startWidth + deltaX, preview.offsetWidth));
        const newHeight = Math.max(200, startHeight + deltaY);

        currentElement.style.width = newWidth + 'px';
        currentElement.style.height = newHeight + 'px';

        showResizeIndicator(currentElement, newWidth, newHeight, e.clientX, e.clientY);
    }

    // 리사이징 중에도 textarea 최신화
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
