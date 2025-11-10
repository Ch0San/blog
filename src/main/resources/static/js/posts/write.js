/*`n * posts/write.js`n * - 글 작성 화면의 라이브 미리보기/툴바 처리 및 파일/이미지 업로드 프리뷰`n * - 임시 업로드 이미지 추적 및 정리, Kakao Maps SDK 동적 주입`n */`n// Kakao Maps SDK 동적 주입`n    var metaEl = document.querySelector('meta[name="kakao-js-key"]');
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

    // 吏꾨떒 濡쒓렇 (??留덉뒪??
    try {
        var masked = key.slice(0, 4) + '***' + key.slice(-4);
        console.log('[Kakao SDK] injecting sdk.js with key=', masked);
    } catch (_) {}
})();

// 二쇱슂 ?섎━癒쇳듃 罹먯떛
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

// ?먮뵒???곹깭
let currentFontFamily = '';
let currentFontSize = '';
let currentColor = '';

// ?낅줈?쒕맂 ?대?吏??異붿쟻
const gathered = [];           // ?쒕쾭?먯꽌 ?뚮젮以 url??紐⑥븘 input[name=imageUrls]濡?蹂대궪 由ъ뒪??
const uploadedImages = new Set(); // ?꾩떆 ?낅줈?쒕맂 URL??
let isCancelling = false;
let cleanupSent = false;

// 珥덇린 ?명똿
preview.setAttribute('contenteditable', 'true');
preview.classList.add('editing');
preview.focus();

// ?먮뵒?곗뿉???댁슜 諛붾??뚮쭏??textarea???숆린??
preview.addEventListener('input', () => {
    textarea.value = preview.innerHTML;
});

// === ?몃꽕??吏??踰꾪듉 濡쒖쭅 ===
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

                // ?뚯씪濡??좏깮 以묒씠?덈떎硫??뚯씪 ?좏깮 ?댁젣
                try {
                    if (thumbnailFileInput) thumbnailFileInput.value = '';
                } catch (_) {}

                // ?몃꽕??誘몃━蹂닿린 媛깆떊
                try {
                    if (thumbnailPreview) {
                        thumbnailPreview.innerHTML = '';
                        const wrapper = document.createElement('div');
                        wrapper.className = 'current-thumbnail';

                        const label = document.createElement('span');
                        label.className = 'thumbnail-preview-label';
                        label.textContent = '현재 썸네일';

                        const thumbImg = document.createElement('img');
                        thumbImg.className = 'thumbnail-preview-image';
                        thumbImg.src = img.src;
                        thumbImg.alt = '현재 썸네일';

                        wrapper.appendChild(label);
                        wrapper.appendChild(thumbImg);
                        thumbnailPreview.appendChild(wrapper);
                    }
                } catch (err) {
                    console.warn('?몃꽕??誘몃━蹂닿린 媛깆떊 ?ㅽ뙣', err);
                }

                alert('?몃꽕?쇰줈 ?깅줉?섏뿀?듬땲??');
            }
        });

        preview.style.position = 'relative';
        preview.appendChild(thumbnailBtn);
    }
});

preview.addEventListener('mouseout', (e) => {
    if (e.target.tagName === 'IMG') {
        if (thumbnailBtn && e.relatedTarget === thumbnailBtn) {
            return; // 踰꾪듉 ?꾨줈 留덉슦???щ씪媛붿쑝硫??좎?
        }
        if (thumbnailBtn && thumbnailBtn.parentNode) {
            thumbnailBtn.remove();
            thumbnailBtn = null;
        }
    }
});

// === 泥⑤? ?뚯씪(?ъ씠?쒕컮) 誘몃━蹂닿린 ===
if (fileInput) {
    fileInput.addEventListener('change', function () {
        const files = Array.from(this.files || []);
        if (!files.length) {
            previewList.innerHTML = '<p style="color:#666;font-size:13px">?좏깮???뚯씪???놁뒿?덈떎.</p>';
            return;
        }

        previewList.innerHTML = '';
        const max = 10 * 1024 * 1024; // 10MB per file

        files.forEach((f, idx) => {
            if (f.size > max) {
                alert(`?뚯씪(#${idx + 1}) ?ш린媛 10MB瑜?珥덇낵?덉뒿?덈떎.`);
                return;
            }

            const item = document.createElement('div');
            item.style.cssText =
                'padding:8px;border:1px solid #dcdde1;border-radius:6px;background:#f8f9fa;font-size:13px;display:flex;align-items:center;gap:8px';

            const icon = document.createElement('span');
            icon.textContent = '?뱨';
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

// ?몃꽕???뚯씪 ?좏깮 ??誘몃━蹂닿린 諛?URL ?댁젣
if (thumbnailFileInput) {
    thumbnailFileInput.addEventListener('change', function () {
        const file = this.files && this.files[0];
        if (!file) return;

        // 10MB ?쒗븳
        if (file.size > 10 * 1024 * 1024) {
            alert('?대?吏 ?뚯씪 ?ш린媛 10MB瑜?珥덇낵?⑸땲??');
            this.value = '';
            return;
        }

        // URL 湲곕컲 ?몃꽕???쒓굅
        const thumbnailInput = document.getElementById('thumbnailUrl');
        if (thumbnailInput) thumbnailInput.value = '';

        // 誘몃━蹂닿린 媛깆떊
        try {
            if (thumbnailPreview) {
                thumbnailPreview.innerHTML = '';
                const wrapper = document.createElement('div');
                wrapper.className = 'current-thumbnail';

                const label = document.createElement('span');
                label.className = 'thumbnail-preview-label';
                label.textContent = '선택한 썸네일';

                const img = document.createElement('img');
                img.className = 'thumbnail-preview-image';
                img.src = URL.createObjectURL(file);
                img.alt = '?좏깮???몃꽕??;

                wrapper.appendChild(label);
                wrapper.appendChild(img);
                thumbnailPreview.appendChild(wrapper);
            }
        } catch (_) {}
    });
}

// === inline(蹂몃Ц) ?대?吏 ?낅줈??===
document.getElementById('inlineImageUpload').addEventListener('change', async (e) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    // 蹂몃Ц ?ъ빱???뺤씤
    const sel = window.getSelection();
    const focusNode = sel.focusNode;
    const isInPreview =
        focusNode &&
        (focusNode === preview || preview.contains(focusNode));

    if (!isInPreview) {
        alert('蹂몃Ц ?곸뿭???대┃?????대?吏瑜??쎌엯?댁＜?몄슂.');
        e.target.value = '';
        return;
    }

    for (const f of files) {
        if (f.size > 4 * 1024 * 1024) {
            alert('?대?吏 ?ш린媛 4MB瑜?珥덇낵?덉뒿?덈떎.');
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
                alert('?낅줈???ㅽ뙣: ' + (data.error || res.status));
                continue;
            }

            const url = data.url;
            const imgHtml =
                `<img src="${url}" alt="image" class="resizable-media" style="max-width:100%;cursor:move" draggable="false"><br>`;

            insertHtmlAtCursor(imgHtml);

            // ?쒕쾭?먯꽌 諛쏆? ?대?吏 URL 異붿쟻
            gathered.push(url);
            uploadedImages.add(url);
            imageUrlsField.value = gathered.join(',');

        } catch (err) {
            alert('?낅줈??以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.');
        }
    }

    e.target.value = '';
});

// === textarea??HTML ?쎌엯 ?꾩슦誘?===
function insertHtmlAtCursor(html) {
    preview.focus();

    let sel = window.getSelection();
    let range;

    // ?좏깮 ?곸뿭??preview 諛붽묑?대㈃ preview ?앹쑝濡???꺼
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

// === ?띿뒪???ㅽ??쇰쭅 愿??===
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
        return true; // ?꾨Т 寃껊룄 ?좏깮 ????寃쎌슦 -> ?댄썑 ??댄븨 湲곕낯 ?ㅽ??쇰쭔 諛붽퓭??
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

// 湲?먯깋
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

// 湲瑗?
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

// 湲???ш린
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

// ?뺣젹
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

// ?대?吏 ?쎌엯 踰꾪듉
if (tbImage) {
    tbImage.addEventListener('click', () => {
        const inlineInput = document.getElementById('inlineImageUpload');
        inlineInput && inlineInput.click();
    });
}

// 鍮꾨뵒???쎌엯 踰꾪듉
if (tbVideo) {
    tbVideo.addEventListener('click', () => {
        const url = prompt('?숈쁺??URL???낅젰?섏꽭??(YouTube, 吏곸젒 ?낅줈????');
        if (!url) return;

        let videoHtml = '';

        // YouTube 異붿텧
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
            // ?쇰컲 鍮꾨뵒??URL
            videoHtml = `
<div class="resizable-media resizable-video"
     style="width:640px;max-width:100%;margin:16px auto;cursor:nwse-resize;display:block">
  <video controls style="width:100%;border-radius:8px;display:block">
    <source src="${url}" type="video/mp4">
    釉뚮씪?곗?媛 鍮꾨뵒?ㅻ? 吏?먰븯吏 ?딆뒿?덈떎.
  </video>
</div>`;
        }

        if (videoHtml) {
            insertHtmlAtCursor(videoHtml);
        } else {
            insertHtmlAtCursor(
                `<p>?숈쁺?? <a href="${url}" target="_blank">${url}</a></p>`
            );
        }
    });
}

// 吏??踰꾪듉
if (tbMap) {
    tbMap.addEventListener('click', openMapModal);
}

// Kakao 吏??SDK 珥덇린???꾩슦誘?
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
                '移댁뭅??吏??SDK 濡쒕뵫 ?ㅽ뙣\n' +
                '- JavaScript ???뺤씤\n' +
                '- ???꾨찓?몄씠 Kakao Developers???깅줉?먮뒗吏 ?뺤씤\n' +
                '- 愿묎퀬李⑤떒 ?뚮윭洹몄씤 李⑤떒 ?щ? ?뺤씤'
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

// 蹂몃Ц??吏??釉붾윮 ?쎌엯
function insertKakaoMapBlock(lat, lng, label) {
    const id = 'kmap-' + Date.now() + '-' + Math.floor(Math.random() * 10000);
    const caption = label
        ? `<div style="font-size:12px;color:#666;margin-top:4px;">?뱧 ${label}</div>`
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

// 吏??紐⑤떖 愿??DOM
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
                center: new kakao.maps.LatLng(37.5665, 126.9780), // ?쒖슱 ?쒖껌 洹쇱쿂
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
        `?좏깮???꾩튂: ${pickedLabel} (${lat.toFixed(5)}, ${lng.toFixed(5)})`;

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
            alert('寃??寃곌낵媛 ?놁뒿?덈떎. 吏?꾨? ?대┃??吏곸젒 ?좏깮?섏꽭??');
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

// === ?꾩떆 ?낅줈?쒕맂 ?대?吏 ?뺣━(sendBeacon) 愿??===
function sendCleanup({ keepUsed }) {
    if (cleanupSent) return;

    const params = new URLSearchParams();
    if (keepUsed) {
        // 蹂몃Ц(html)???ㅼ젣濡??ъ슜???대?吏留??④린怨??섎㉧吏????젣?섎씪怨??뚮┝
        const contentHtml = preview.innerHTML;
        const usedImages = Array.from(uploadedImages)
            .filter(url => contentHtml.includes(url));
        params.append('usedImages', usedImages.join(','));
    } else {
        // 痍⑥냼?섎㈃ ?꾨? ??젣
        params.append('usedImages', '');
        params.append('mode', 'cancel');
    }

    try {
        navigator.sendBeacon('/api/uploads/cleanup', params);
        cleanupSent = true;
    } catch (e) {
        // Beacon???????섎룄 ?덉쑝?덇퉴 fetch fallback
        fetch('/api/uploads/cleanup', {
            method: 'POST',
            body: params,
            keepalive: true
        }).catch(() => {});
        cleanupSent = true;
    }
}

// 痍⑥냼 踰꾪듉 ?뚮????? ?꾩떆 ?낅줈???꾨? 踰꾨━怨?/posts 濡??섍컧
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

// ?섏씠吏 ?댄깉 ?? ?ъ슜 ?????꾩떆 ?대?吏???뺣━
window.addEventListener('beforeunload', () => {
    if (uploadedImages.size > 0 && !cleanupSent) {
        // ?뺤긽 ?쒖텧(submit)濡?醫낅즺?섎뒗 寃쎌슦??cleanupSent瑜?true濡?留뚮뱾嫄곕씪???ш린 ????
        sendCleanup({ keepUsed: !isCancelling });
    }
});

// === ???쒖텧 吏곸쟾 理쒖쥌 ?숆린??===
// ?닿쾶 以묒슂?? preview ?댁슜??textarea濡?蹂듭궗?섍퀬
// cleanupSent=true濡?諛붽퓭???쒕쾭媛 ?꾩떆?뚯씪??吏?곗? ?딄쾶 蹂댄샇
if (postForm) {
    postForm.addEventListener('submit', () => {
        // 蹂몃Ц 理쒖쥌 HTML -> textarea濡?
        textarea.value = preview.innerHTML;

        // "?섎뒗 ?뺤긽 ???以묒씠?? ?쒖떆
        isCancelling = false;
        cleanupSent = true;
    });
}

// === 由ъ궗?댁쭠 濡쒖쭅 (?대?吏/鍮꾨뵒??吏??諛뺤뒪 ?쒕옒洹몃줈 ?ш린 議곗젅) ===
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

    resizeIndicator.textContent = `${Math.round(width)}px 횞 ${Math.round(height)}px`;
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

    // ?대?吏 ?ш린 議곗젅
    if (target.tagName === 'IMG' && target.classList.contains('resizable-media')) {
        isResizing = true;
        currentElement = target;
        startX = e.clientX;
        startY = e.clientY;
        startWidth = target.offsetWidth;
        e.preventDefault();
    }

    // 鍮꾨뵒??而⑦뀒?대꼫(iframe wrapper??video wrapper)
    if (target.closest('.resizable-video')) {
        const videoContainer = target.closest('.resizable-video');
        isResizing = true;
        currentElement = videoContainer;
        startX = e.clientX;
        startY = e.clientY;
        startWidth = videoContainer.offsetWidth;
        e.preventDefault();
    }

    // 吏??而⑦뀒?대꼫
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
        // ?대?吏
        const newWidth = Math.max(100, Math.min(startWidth + deltaX, preview.offsetWidth));
        currentElement.style.width = newWidth + 'px';
        currentElement.style.maxWidth = 'none';

        showResizeIndicator(currentElement, newWidth, currentElement.offsetHeight, e.clientX, e.clientY);
    } else if (currentElement.classList.contains('resizable-video')) {
        // 鍮꾨뵒??(16:9 媛??
        const newWidth = Math.max(200, Math.min(startWidth + deltaX, preview.offsetWidth));
        const newHeight = newWidth * 9 / 16;
        currentElement.style.width = newWidth + 'px';

        showResizeIndicator(currentElement, newWidth, newHeight, e.clientX, e.clientY);
    } else if (currentElement.classList.contains('resizable-map')) {
        // 吏??諛뺤뒪 (width/height ????
        const deltaY = e.clientY - startY;
        const newWidth = Math.max(200, Math.min(startWidth + deltaX, preview.offsetWidth));
        const newHeight = Math.max(200, startHeight + deltaY);

        currentElement.style.width = newWidth + 'px';
        currentElement.style.height = newHeight + 'px';

        showResizeIndicator(currentElement, newWidth, newHeight, e.clientX, e.clientY);
    }

    // 由ъ궗?댁쭠 以묒뿉??textarea 理쒖떊??
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




