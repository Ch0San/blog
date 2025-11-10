/*\n * stories/list.js\n * - 목록 카드에서 썸네일 이미지 ↔ 동영상 hover 프리뷰 전환\n * - 마우스 진입 지연(1s) 후 시작, 최대 5초 재생 후 정지/복구\n */\n// ?숈쁺??誘몃━蹂닿린 諛??몃꽕???꾪솚 濡쒖쭅
document.addEventListener('DOMContentLoaded', function () {
    const storyThumbnails = document.querySelectorAll('.stories-thumbnail');

    storyThumbnails.forEach(thumbnailDiv => {
        /** @type {HTMLVideoElement|null} */
        const video = thumbnailDiv.querySelector('.video-thumbnail');
        /** @type {HTMLImageElement|null} */
        const img = thumbnailDiv.querySelector('.thumbnail-img');

        let hoverTimer = null;
        let checkTime = null;
        let playPromise = null;
        let isPlaying = false;

        // 鍮꾨뵒?ㅺ? ?꾩삁 ?놁쑝硫??대?吏留??덈뒗 移대뱶) ???꾨Т ?숈옉 ????
        if (!video) {
            return;
        }

        //
        // 珥덇린 ?곹깭 ?명똿
        // - ?대?吏+鍮꾨뵒??移대뱶硫? ?대?吏留?蹂댁씠寃?(img opacity 1 / video opacity 0)
        // - 鍮꾨뵒?ㅻ쭔 ?덈뒗 移대뱶硫? 鍮꾨뵒?ㅼ쓽 泥??꾨젅??0.1s)???몃꽕?쇱쿂??蹂댁뿬二쇨린
        //
        function showFirstFrameAsThumb() {
            try {
                // ?댁쭩 0.1珥덈줈 ?↔꺼??寃? ?붾㈃ 諛⑹?
                video.currentTime = 0.1;
            } catch (_) {
                // metadata媛 ?꾩쭅 ??遺덈윭?ㅻ㈃ ?먮윭 ?????덉쓬 ??臾댁떆
            }

            video.pause();
            isPlaying = false;

            if (img) {
                // ?대?吏媛 ?덈뒗 寃쎌슦 ??鍮꾨뵒?ㅻ뒗 湲곕낯 ?④?
                video.style.opacity = '0';
                video.style.pointerEvents = 'none';

                img.style.opacity = '1';
                if (!img.style.transition) {
                    img.style.transition = 'opacity .15s';
                }
            } else {
                // ?대?吏媛 ?녿뒗 寃쎌슦 ??鍮꾨뵒???먯껜媛 ?몃꽕????븷
                video.style.opacity = '1';
                video.style.pointerEvents = 'none';
            }
        }

        // metadata媛 ?대? 以鍮꾨맂 寃쎌슦/以鍮??덈맂 寃쎌슦 紐⑤몢 ???
        if (video.readyState >= 1) {
            showFirstFrameAsThumb();
        } else {
            video.addEventListener('loadedmetadata', showFirstFrameAsThumb, { once: true });
        }

        //
        // hover ?쒖옉: 1珥??좎??섎㈃ ?꾨━酉??ъ깮
        //
        thumbnailDiv.addEventListener('mouseenter', function () {
            // ?대? ??대㉧媛 ?뚭퀬 ?덈떎硫?以묐났?쇰줈 ??留뚮뱾 ?꾩슂 X
            if (hoverTimer) return;

            hoverTimer = setTimeout(() => {
                // ?몃꽕???대?吏媛 ?덈떎硫? 鍮꾨뵒?ㅺ? ?щ씪?ㅻ㈃???대?吏???섏씠?쒖븘??
                if (img) {
                    img.style.opacity = '0';
                    img.style.transition = 'opacity .15s';
                }

                // 鍮꾨뵒??蹂댁씠寃??꾪솚
                video.style.opacity = '1';
                video.style.pointerEvents = 'auto';

                // 泥섏쓬遺???ъ깮
                video.currentTime = 0;

                playPromise = video.play().then(() => {
                    isPlaying = true;

                    // 理쒕? 5珥덇퉴吏留??ъ깮
                    checkTime = setInterval(() => {
                        if (video.currentTime >= 5) {
                            stopPreviewAndRestore();
                        }
                    }, 100);
                }).catch(() => {
                    // ?먮룞 ?ъ깮??釉뚮씪?곗? ?뺤콉 ?깆쑝濡?嫄곕???寃쎌슦
                    isPlaying = false;
                    restoreStillFrame();
                });
            }, 1000); // 1珥??쒕젅??
        });

        //
        // hover 醫낅즺: 利됱떆 蹂듦뎄
        //
        thumbnailDiv.addEventListener('mouseleave', function () {
            // ?꾩쭅 ?ъ깮 ?湲?以?1珥???대㉧留????곹깭)???섎룄 ?덉쓬 ????대㉧ ?뺣━
            if (hoverTimer) {
                clearTimeout(hoverTimer);
                hoverTimer = null;
            }

            if (playPromise) {
                // play()??鍮꾨룞湲곕씪 then ?댄썑??pause?댁빞 ?덉쟾
                playPromise.then(() => {
                    stopPreviewAndRestore();
                });
                playPromise = null;
            } else if (isPlaying) {
                // ?대? ?ъ깮 以묒씠?쇰㈃ 利됱떆 ?뺤?
                stopPreviewAndRestore();
            } else {
                // ?꾩삁 ?ъ깮 ?????곹깭?쇰㈃ ?몃꽕?쇰쭔 蹂듦뎄
                restoreStillFrame();
            }
        });

        //
        // 怨듯넻 ?좏떥: ?꾨━酉??꾩쟾??以묐떒?섍퀬 ?몃꽕??蹂듦?
        //
        function stopPreviewAndRestore() {
            if (checkTime) {
                clearInterval(checkTime);
                checkTime = null;
            }

            video.pause();
            video.currentTime = 0;
            isPlaying = false;

            restoreStillFrame();
        }

        //
        // ?몃꽕???곹깭濡??섎룎由щ뒗 泥섎━
        //
        function restoreStillFrame() {
            // 鍮꾨뵒?ㅻ? ?ㅼ떆 "?뺤????몃꽕??紐⑤뱶"濡?
            try {
                video.currentTime = 0.1;
            } catch (_) { }

            video.pause();
            video.style.pointerEvents = 'none';

            if (img) {
                // ?대?吏媛 ?덈뒗 移대뱶?쇰㈃ ?대?吏媛 ?ㅼ떆 蹂댁씠怨?
                // 鍮꾨뵒?ㅻ뒗 ?댁쭩 ?④?
                img.style.opacity = '1';
                video.style.opacity = '0';
            } else {
                // 鍮꾨뵒?ㅻ쭔 ?덈뒗 移대뱶?쇰㈃ 鍮꾨뵒?ㅺ? 怨꾩냽 ?몃꽕????븷
                video.style.opacity = '1';
            }
        }
    });
});


