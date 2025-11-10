/*\n * posts/edit.js\n * - 글 수정 화면의 라이브 미리보기/툴바(굵게/기울임/정렬/색상/폰트) 처리\n * - 이미지/썸네일 미리보기, 썸네일 버튼(이미지 hover) 등록\n * - Kakao Maps SDK 동적 주입\n */\n// Kakao Maps SDK 동적 주입
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
    // 吏꾨떒?? 肄섏넄??URL ?쇰? 濡쒓렇(??留덉뒪??
    try {
        var masked = key.slice(0, 4) + '***' + key.slice(-4);
        console.log('[Kakao SDK] injecting sdk.js with key=', masked);
    } catch (e) { }
})();

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

// 현재 ?좏깮???쒖떇 ?곹깭 ???
let currentFontFamily = '';
let currentFontSize = '';
let currentColor = '';

// 誘몃━蹂닿린 ?뺤떇?먯꽌 諛붾줈 ?묒꽦 (怨좎젙 紐⑤뱶)
preview.setAttribute('contenteditable', 'true');
preview.classList.add('editing');

// 湲곗〈 ?댁슜??誘몃━蹂닿린??濡쒕뱶
if (textarea.value && textarea.value.trim()) {
    preview.innerHTML = textarea.value;
}
preview.focus();

// 誘몃━蹂닿린?먯꽌 ??댄븨?섎㈃ ?⑥? textarea??HTML ?숆린??(???꾩넚??
preview.addEventListener('input', () => {
    textarea.value = preview.innerHTML;
});

// ?먮뵒?????대?吏??留덉슦???ㅻ쾭 ???몃꽕??吏??踰꾪듉 ?쒖떆
let thumbnailBtn = null;

preview.addEventListener('mouseover', (e) => {
    if (e.target.tagName === 'IMG') {
        // 湲곗〈 踰꾪듉 ?쒓굅
        if (thumbnailBtn && thumbnailBtn.parentNode) {
            thumbnailBtn.remove();
        }

        const img = e.target;
        const rect = img.getBoundingClientRect();
        const previewRect = preview.getBoundingClientRect();

        // ?몃꽕??踰꾪듉 ?앹꽦
        thumbnailBtn = document.createElement('button');
        thumbnailBtn.textContent = '?몃꽕??;
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
                // URL濡??몃꽕??吏???? ?뚯씪 ?낅젰 鍮꾩슦怨???젣 泥댄겕 ?댁젣
                try {
                    if (thumbnailFileInput) thumbnailFileInput.value = '';
                    const deleteThumbnailCheckbox = document.getElementById('deleteThumbnail');
                    if (deleteThumbnailCheckbox) deleteThumbnailCheckbox.checked = false;
                } catch (_) {}
                // 誘몃━蹂닿린 媛깆떊
                try {
                    const previewBox = document.getElementById('thumbnailPreview');
                    if (previewBox) {
                        previewBox.innerHTML = '';
                        const wrapper = document.createElement('div');
                        wrapper.className = 'current-thumbnail';
                        const label = document.createElement('span');
                        label.className = 'thumbnail-preview-label';
                        label.textContent = '현재 ?몃꽕??';
                        const thumbImg = document.createElement('img');
                        thumbImg.className = 'thumbnail-preview-image';
                        thumbImg.src = img.src;
                        thumbImg.alt = '현재 ?몃꽕??;
                        wrapper.appendChild(label);
                        wrapper.appendChild(thumbImg);
                        previewBox.appendChild(wrapper);
                    }
                } catch (e) { console.warn('?몃꽕??誘몃━蹂닿린 媛깆떊 ?ㅽ뙣', e); }
                alert('?몃꽕??URL濡??깅줉?섏뿀?듬땲?? ' + img.src);
            }
        });

        preview.style.position = 'relative';
        preview.appendChild(thumbnailBtn);
    }
});

preview.addEventListener('mouseout', (e) => {
    if (e.target.tagName === 'IMG') {
        // 踰꾪듉 ?곸뿭?쇰줈 留덉슦?ㅺ? ?대룞??寃쎌슦???좎?
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
            item.style.cssText = 'padding:8px;border:1px solid #dcdde1;border-radius:6px;background:#f8f9fa;font-size:13px;display:flex;align-items:center;gap:8px';

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

// ?몃꽕???대?吏 誘몃━蹂닿린 (?몃꽕???뚯씪 援먯껜 ???숈옉)
if (thumbnailFileInput) {
    thumbnailFileInput.addEventListener('change', function () {
        const deleteThumbnailCheckbox = document.getElementById('deleteThumbnail');
        const file = this.files[0];

        if (!file) return;

        // ???뚯씪???좏깮?섎㈃ ??젣 泥댄겕諛뺤뒪 ?먮룞 ?댁젣
        if (deleteThumbnailCheckbox) {
            deleteThumbnailCheckbox.checked = false;
        }

        thumbnailPreview.innerHTML = '';

        // ?뚯씪 ?ш린 泥댄겕 (10MB)
        if (file.size > 10 * 1024 * 1024) {
            alert('?대?吏 ?뚯씪 ?ш린媛 10MB瑜?珥덇낵?⑸땲??\n현재 ?뚯씪 ?ш린: ' + (file.size / 1024 / 1024).toFixed(2) + 'MB');
            this.value = '';
            return;
        }

        // URL 湲곕컲 ?몃꽕?쇱쓣 ?ъ슜 以묒씠?덈떎硫?珥덇린??        const thumbnailInput = document.getElementById('thumbnailUrl');
        if (thumbnailInput) thumbnailInput.value = '';

        // 誘몃━蹂닿린 ?쒖떆
        const previewContainer = document.createElement('div');
        previewContainer.style.cssText = 'border:1px solid #e6e8eb;border-radius:6px;padding:10px;background:#f8f9fa';

        const fileInfo = document.createElement('div');
        fileInfo.innerHTML = `
            <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px">
                <span style="font-size:20px">?뼹截?/span>
                <div>
                    <div style="font-weight:500;color:var(--text)">${file.name}</div>
                    <div style="font-size:12px;color:var(--muted)">${(file.size / 1024 / 1024).toFixed(2)} MB</div>
                </div>
            </div>
        `;

        // ?대?吏 誘몃━蹂닿린
        const img = document.createElement('img');
        img.style.cssText = 'width:100%;max-width:400px;border-radius:6px';
        img.src = URL.createObjectURL(file);

        previewContainer.appendChild(fileInfo);
        previewContainer.appendChild(img);
        thumbnailPreview.appendChild(previewContainer);
    });
}

// ??젣 泥댄겕諛뺤뒪 ?대깽??
const deleteThumbnailCheckbox = document.getElementById('deleteThumbnail');
if (deleteThumbnailCheckbox) {
    deleteThumbnailCheckbox.addEventListener('change', function () {
        const currentThumbnail = document.getElementById('currentThumbnail');
        if (this.checked) {
            // 泥댄겕?섎㈃ 현재 ?몃꽕?쇱뿉 諛섑닾紐??④낵
            if (currentThumbnail) {
                currentThumbnail.classList.add('delete-pending');
                const overlay = document.createElement('div');
                overlay.id = 'deleteOverlay';
                overlay.className = 'delete-overlay';
                overlay.innerHTML = '<span class="delete-badge">??젣 ?덉젙</span>';
                currentThumbnail.appendChild(overlay);
            }
            // ?뚯씪 ?좏깮 珥덇린??            if (thumbnailFileInput) thumbnailFileInput.value = '';
            // URL 湲곕컲 ?몃꽕?쇰룄 ?댁젣
            const thumbnailInput = document.getElementById('thumbnailUrl');
            if (thumbnailInput) thumbnailInput.value = '';
        } else {
            // 泥댄겕 ?댁젣?섎㈃ ?먮옒?濡?            if (currentThumbnail) {
                currentThumbnail.classList.remove('delete-pending');
                const overlay = document.getElementById('deleteOverlay');
                if (overlay) overlay.remove();
            }
        }
    });
}

// ???쒖텧 吏곸쟾: 蹂몃Ц HTML ?숆린??蹂닿컯 (?뱀떆 ?꾨씫 諛⑹?)
document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('form.editor-form');
    if (form) {
        form.addEventListener('submit', () => {
            if (preview && textarea) {
                textarea.value = preview.innerHTML;
            }
        });
    }
});

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

// contenteditable??HTML ?쎌엯 (蹂몃Ц ?곸뿭?먮쭔)
function insertHtmlAtCursor(html) {
    // ?먮뵒??蹂몃Ц???ъ빱???ㅼ젙
    preview.focus();

    let sel, range;
    if (window.getSelection) {
        sel = window.getSelection();

        // ?좏깮 ?곸뿭??preview ?대??몄? ?뺤씤
        if (sel.rangeCount > 0) {
            range = sel.getRangeAt(0);
            const container = range.commonAncestorContainer;
            const isInPreview = preview.contains(container.nodeType === 3 ? container.parentNode : container);

            // preview ?대?媛 ?꾨땲硫?preview ?앹뿉 ?쎌엯
            if (!isInPreview) {
                range = document.createRange();
                range.selectNodeContents(preview);
                range.collapse(false); // ?앹쑝濡??대룞
                sel.removeAllRanges();
                sel.addRange(range);
            }
        } else {
            // ?좏깮 ?곸뿭???놁쑝硫?preview ?앹뿉 ?쎌엯
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
    // textarea?먮룄 ?숆린??
    textarea.value = preview.innerHTML;
}

// ?먮뵒?곗뿉 ?낅젰 ??현재 ?쒖떇 ?곸슜
preview.addEventListener('keypress', (e) => {
    if (currentFontFamily || currentFontSize || currentColor) {
        e.preventDefault();

        const char = e.key;
        if (char.length === 1) { // ?쇰컲 臾몄옄留?
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

// ?띿뒪???쒖떇 ?곸슜 ?⑥닔
function wrapSelectionWithTag(tagName, styles = {}) {
    const sel = window.getSelection();
    if (!sel.rangeCount) return;

    const range = sel.getRangeAt(0);
    const selectedText = range.toString();

    if (!selectedText) {
        // ?띿뒪???좏깮 ?놁쑝硫??욎쑝濡??낅젰???띿뒪?몄뿉 ?곸슜
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

        // 而ㅼ꽌瑜?span ?ㅻ줈 ?대룞
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

// ?띿뒪???쒖떇 ?대컮 ?숈옉
if (tbBold) {
    tbBold.addEventListener('click', () => {
        preview.focus();
        document.execCommand('bold', false, null);
        textarea.value = preview.innerHTML;
        // ?쒖꽦 ?곹깭 ?좉?
        tbBold.classList.toggle('active');
    });
}
if (tbItalic) {
    tbItalic.addEventListener('click', () => {
        preview.focus();
        document.execCommand('italic', false, null);
        textarea.value = preview.innerHTML;
        // ?쒖꽦 ?곹깭 ?좉?
        tbItalic.classList.toggle('active');
    });
}
if (tbUnderline) {
    tbUnderline.addEventListener('click', () => {
        preview.focus();
        document.execCommand('underline', false, null);
        textarea.value = preview.innerHTML;
        // ?쒖꽦 ?곹깭 ?좉?
        tbUnderline.classList.toggle('active');
    });
}
// 湲???됱긽
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
                // ?좏깮???띿뒪???놁쑝硫??쒖떇 ?곸슜 ?쒖떆
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
                // ?좏깮???띿뒪???놁쑝硫??쒖떇 ?곸슜 ?쒖떆
                tbFontSize.style.backgroundColor = '#e3f2fd';
            }
        } else {
            currentFontSize = '';
            tbFontSize.style.backgroundColor = '#fff';
        }
    });
}

// ?뺣젹 ?곸슜 (?좏깮 ?곸뿭??釉붾줉?쇰줈 媛먯떥嫄곕굹, 鍮?釉붾줉 ?쎌엯)
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
            // 而ㅼ꽌瑜?wrapper ?ㅻ줈 ?대룞
            sel.removeAllRanges();
            const newRange = document.createRange();
            newRange.setStartAfter(wrapper);
            newRange.collapse(true);
            sel.addRange(newRange);
        } else {
            // ?좏깮???놁쑝硫??뺣젹 釉붾줉 ?쎌엯
            insertHtmlAtCursor(`<div style=\"text-align:${align}\"><br></div>`);
        }
        textarea.value = preview.innerHTML;
    } catch (ex) {
        console.error('applyAlignment error:', ex);
    }
}
if (tbAlignLeft) tbAlignLeft.addEventListener('click', () => applyAlignment('left'));
if (tbAlignCenter) tbAlignCenter.addEventListener('click', () => applyAlignment('center'));
if (tbAlignRight) tbAlignRight.addEventListener('click', () => applyAlignment('right'));

// ?대컮 ?숈옉
if (tbImage) {
    tbImage.addEventListener('click', () => {
        const inlineInput = document.getElementById('inlineImageUpload');
        inlineInput && inlineInput.click();
    });
}
if (tbVideo) {
    tbVideo.addEventListener('click', () => {
        const url = prompt('?숈쁺??URL???낅젰?섏꽭??(YouTube, 吏곸젒 ?낅줈????');
        if (url) {
            let videoHtml = '';
            // YouTube URL 泥섎━
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
                // ?쇰컲 鍮꾨뵒??URL (mp4, webm ??
                videoHtml = `<div class="resizable-media resizable-video" style="width:640px;max-width:100%;margin:16px auto;cursor:nwse-resize;display:block"><video controls style="width:100%;border-radius:8px;display:block"><source src="${url}" type="video/mp4">釉뚮씪?곗?媛 鍮꾨뵒?ㅻ? 吏?먰븯吏 ?딆뒿?덈떎.</video></div>`;
            }
            if (videoHtml) {
                insertHtmlAtCursor(videoHtml);
            } else {
                insertHtmlAtCursor(`<p>?숈쁺?? <a href="${url}" target="_blank">${url}</a></p>`);
            }
        }
    });
}

// Kakao 吏?? SDK 以鍮??湲?
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
            alert('移댁뭅??吏??SDK 濡쒕뵫 ?ㅽ뙣\n- JavaScript ???꾨찓???깅줉???뺤씤?섏꽭??');
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
    const caption = label ? `<div style=\"font-size:12px;color:#666;margin-top:4px;\">?뱧 ${label}</div>` : '';
    const html = `\n<div class=\"kmap resizable-media\" style=\"margin:8px 0;max-width:100%;width:100%\">\n  <div id=\"${id}\" class=\"resizable-map\" style=\"width:100%;height:320px;border-radius:8px;border:1px solid #e6e8eb;cursor:nwse-resize;\"></div>\n  ${caption}\n</div>\n<script>(function(){if(!window.kakao||!kakao.maps){return;}var c=document.getElementById('${id}');if(!c)return;var map=new kakao.maps.Map(c,{center:new kakao.maps.LatLng(${lat},${lng}),level:3});var marker=new kakao.maps.Marker({position:new kakao.maps.LatLng(${lat},${lng})});marker.setMap(map);}());<\/script>\n`;
    insertHtmlAtCursor(html);
    ensureKakaoReady(() => renderKakaoMap(id, lat, lng));
}

// 吏???좏깮 紐⑤떖 濡쒖쭅 (?섏젙)
const mapModal = document.getElementById('mapModalEdit');
const mapBackdrop = document.getElementById('mapModalBackdropEdit');
const mapClose = document.getElementById('mapModalCloseEdit');
const mapInsertBtn = document.getElementById('mapInsertBtnEdit');
const mapSearchInput = document.getElementById('mapSearchInputEdit');
const mapSearchBtn = document.getElementById('mapSearchBtnEdit');
const mapPickedInfo = document.getElementById('mapPickedInfoEdit');
let pickerMap, pickerMarker, pickedLatLng, pickedLabel;

function openMapModal() {
    mapBackdrop.style.display = 'block';
    mapModal.style.display = 'block';
    ensureKakaoReady(() => {
        if (!pickerMap) {
            const container = document.getElementById('kmap-picker-edit');
            pickerMap = new kakao.maps.Map(container, { center: new kakao.maps.LatLng(37.5665, 126.9780), level: 5 });
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
    mapPickedInfo.textContent = `?좏깮???꾩튂: ${pickedLabel} (${lat.toFixed(5)}, ${lng.toFixed(5)})`;
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
            alert('寃??寃곌낵媛 ?놁뒿?덈떎. 吏?꾨? ?대┃??吏곸젒 ?좏깮?섏꽭??');
        }
    });
}
if (tbMap) { tbMap.addEventListener('click', openMapModal); }
mapClose.addEventListener('click', closeMapModal);
mapBackdrop.addEventListener('click', closeMapModal);
mapSearchBtn.addEventListener('click', () => keywordSearch(mapSearchInput.value.trim()));
mapSearchInput.addEventListener('keydown', (e) => { if (e.key === 'Enter') { e.preventDefault(); keywordSearch(mapSearchInput.value.trim()); } });
mapInsertBtn.addEventListener('click', () => { if (!pickedLatLng) return; insertKakaoMapBlock(pickedLatLng.lat, pickedLatLng.lng, pickedLabel); closeMapModal(); });

// AJAX ?낅줈???ㅼ젙
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content') || 'X-CSRF-TOKEN';
const imageUrlsField = document.getElementById('imageUrls');
const gathered = [];

// ?낅줈?쒕맂 ?대?吏 異붿쟻
const uploadedImages = new Set();
let isCancelling = false;
let cleanupSent = false;

function sendCleanup({ keepUsed }) {
    if (cleanupSent) return;
    const params = new URLSearchParams();
    if (keepUsed) {
        const contentHtml = preview.innerHTML;
        const usedImages = Array.from(uploadedImages).filter(url => contentHtml.includes(url));
        params.append('usedImages', usedImages.join(','));
    } else {
        params.append('usedImages', '');
        params.append('mode', 'cancel');
    }
    try {
        navigator.sendBeacon('/api/uploads/cleanup', params);
        cleanupSent = true;
    } catch (e) {
        fetch('/api/uploads/cleanup', { method: 'POST', body: params, keepalive: true }).catch(() => { });
        cleanupSent = true;
    }
}

document.getElementById('inlineImageUpload').addEventListener('change', async (e) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    // 蹂몃Ц ?곸뿭???ъ빱?ㅺ? ?놁쑝硫?寃쎄퀬
    const sel = window.getSelection();
    const focusNode = sel.focusNode;
    const isInPreview = focusNode && (focusNode === preview || preview.contains(focusNode));

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
            const imgHtml = `<img src="${url}" alt="image" class="resizable-media" style="max-width:100%;cursor:move" draggable="false"><br>`;
            insertHtmlAtCursor(imgHtml);
            gathered.push(url);
            uploadedImages.add(url); // 異붿쟻 紐⑸줉??異붽?
            imageUrlsField.value = gathered.join(',');
        } catch (err) {
            alert('?낅줈??以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.');
        }
    }
    e.target.value = '';
});

// ?섏씠吏 ?댄깉 ???ъ슜?섏? ?딆? ?꾩떆 ?대?吏 ?뺣━
window.addEventListener('beforeunload', () => {
    if (uploadedImages.size > 0 && !cleanupSent) {
        sendCleanup({ keepUsed: !isCancelling });
    }
});

// 痍⑥냼 踰꾪듉 ?대┃ ??紐⑤뱺 ?꾩떆 ?낅줈????젣 ???곸꽭 ?붾㈃?쇰줈 ?대룞
document.addEventListener('DOMContentLoaded', () => {
    const cancelBtn = document.querySelector('.form-actions .btn.btn-secondary');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', (e) => {
            if (uploadedImages.size > 0) {
                e.preventDefault();
                e.stopPropagation();
                isCancelling = true;
                sendCleanup({ keepUsed: false });
                const href = cancelBtn.getAttribute('href') || window.location.pathname.replace('/edit', '');
                setTimeout(() => { window.location.href = href; }, 60);
            }
        });
    }
});

// 誘몃뵒??由ъ궗?댁쭠 湲곕뒫
let isResizing = false;
let currentElement = null;
let startX, startY, startWidth, startHeight;
let resizeIndicator = null;

// ?ш린 ?쒖떆 ?몃뵒耳?댄꽣 ?앹꽦
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

    // ?대?吏 由ъ궗?댁쭠
    if (target.tagName === 'IMG' && target.classList.contains('resizable-media')) {
        isResizing = true;
        currentElement = target;
        startX = e.clientX;
        startY = e.clientY;
        startWidth = target.offsetWidth;
        e.preventDefault();
    }

    // 鍮꾨뵒??而⑦뀒?대꼫 由ъ궗?댁쭠 (YouTube iframe wrapper ?먮뒗 video wrapper)
    if (target.closest('.resizable-video')) {
        const videoContainer = target.closest('.resizable-video');
        isResizing = true;
        currentElement = videoContainer;
        startX = e.clientX;
        startY = e.clientY;
        startWidth = videoContainer.offsetWidth;
        e.preventDefault();
    }

    // 吏??由ъ궗?댁쭠
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
        // ?대?吏 ?ш린 議곗젅
        const newWidth = Math.max(100, Math.min(startWidth + deltaX, preview.offsetWidth));
        currentElement.style.width = newWidth + 'px';
        currentElement.style.maxWidth = 'none';
        showResizeIndicator(currentElement, newWidth, currentElement.offsetHeight, e.clientX, e.clientY);
    } else if (currentElement.classList.contains('resizable-video')) {
        // 鍮꾨뵒???ш린 議곗젅
        const newWidth = Math.max(200, Math.min(startWidth + deltaX, preview.offsetWidth));
        const newHeight = newWidth * 9 / 16;
        currentElement.style.width = newWidth + 'px';
        showResizeIndicator(currentElement, newWidth, newHeight, e.clientX, e.clientY);
    } else if (currentElement.classList.contains('resizable-map')) {
        // 吏???ш린 議곗젅 (?덈퉬? ?믪씠)
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



