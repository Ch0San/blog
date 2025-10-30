// 동영상 미리보기 및 썸네일 전환 로직
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

        // 비디오가 아예 없으면(이미지만 있는 카드) → 아무 동작 안 함
        if (!video) {
            return;
        }

        //
        // 초기 상태 세팅
        // - 이미지+비디오 카드면: 이미지만 보이게 (img opacity 1 / video opacity 0)
        // - 비디오만 있는 카드면: 비디오의 첫 프레임(0.1s)을 썸네일처럼 보여주기
        //
        function showFirstFrameAsThumb() {
            try {
                // 살짝 0.1초로 땡겨서 검은 화면 방지
                video.currentTime = 0.1;
            } catch (_) {
                // metadata가 아직 안 불러오면 에러 날 수 있음 → 무시
            }

            video.pause();
            isPlaying = false;

            if (img) {
                // 이미지가 있는 경우 → 비디오는 기본 숨김
                video.style.opacity = '0';
                video.style.pointerEvents = 'none';

                img.style.opacity = '1';
                if (!img.style.transition) {
                    img.style.transition = 'opacity .15s';
                }
            } else {
                // 이미지가 없는 경우 → 비디오 자체가 썸네일 역할
                video.style.opacity = '1';
                video.style.pointerEvents = 'none';
            }
        }

        // metadata가 이미 준비된 경우/준비 안된 경우 모두 대응
        if (video.readyState >= 1) {
            showFirstFrameAsThumb();
        } else {
            video.addEventListener('loadedmetadata', showFirstFrameAsThumb, { once: true });
        }

        //
        // hover 시작: 1초 유지하면 프리뷰 재생
        //
        thumbnailDiv.addEventListener('mouseenter', function () {
            // 이미 타이머가 돌고 있다면 중복으로 또 만들 필요 X
            if (hoverTimer) return;

            hoverTimer = setTimeout(() => {
                // 썸네일 이미지가 있다면, 비디오가 올라오면서 이미지는 페이드아웃
                if (img) {
                    img.style.opacity = '0';
                    img.style.transition = 'opacity .15s';
                }

                // 비디오 보이게 전환
                video.style.opacity = '1';
                video.style.pointerEvents = 'auto';

                // 처음부터 재생
                video.currentTime = 0;

                playPromise = video.play().then(() => {
                    isPlaying = true;

                    // 최대 5초까지만 재생
                    checkTime = setInterval(() => {
                        if (video.currentTime >= 5) {
                            stopPreviewAndRestore();
                        }
                    }, 100);
                }).catch(() => {
                    // 자동 재생이 브라우저 정책 등으로 거부된 경우
                    isPlaying = false;
                    restoreStillFrame();
                });
            }, 1000); // 1초 딜레이
        });

        //
        // hover 종료: 즉시 복구
        //
        thumbnailDiv.addEventListener('mouseleave', function () {
            // 아직 재생 대기 중(1초 타이머만 돈 상태)일 수도 있음 → 타이머 정리
            if (hoverTimer) {
                clearTimeout(hoverTimer);
                hoverTimer = null;
            }

            if (playPromise) {
                // play()는 비동기라 then 이후에 pause해야 안전
                playPromise.then(() => {
                    stopPreviewAndRestore();
                });
                playPromise = null;
            } else if (isPlaying) {
                // 이미 재생 중이라면 즉시 정지
                stopPreviewAndRestore();
            } else {
                // 아예 재생 안 된 상태라면 썸네일만 복구
                restoreStillFrame();
            }
        });

        //
        // 공통 유틸: 프리뷰 완전히 중단하고 썸네일 복귀
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
        // 썸네일 상태로 되돌리는 처리
        //
        function restoreStillFrame() {
            // 비디오를 다시 "정지된 썸네일 모드"로
            try {
                video.currentTime = 0.1;
            } catch (_) { }

            video.pause();
            video.style.pointerEvents = 'none';

            if (img) {
                // 이미지가 있는 카드라면 이미지가 다시 보이고
                // 비디오는 살짝 숨김
                img.style.opacity = '1';
                video.style.opacity = '0';
            } else {
                // 비디오만 있는 카드라면 비디오가 계속 썸네일 역할
                video.style.opacity = '1';
            }
        }
    });
});
