// 동영상 미리보기 및 썸네일 대체 기능
document.addEventListener('DOMContentLoaded', function () {
    const storyThumbnails = document.querySelectorAll('.stories-thumbnail');
    storyThumbnails.forEach(thumbnailDiv => {
        const video = thumbnailDiv.querySelector('.video-thumbnail');
        const img = thumbnailDiv.querySelector('img');
        let hoverTimer = null;
        let isPlaying = false;
        let checkTime = null;

        // 썸네일만 있는 경우: 동영상 미리보기 없음
        if (!video && img) return;

        // 동영상만 있는 경우: 첫 프레임을 썸네일처럼 보이게
        if (video && !img) {
            video.currentTime = 0.1;
            video.pause();
            video.style.opacity = '1';
            video.style.pointerEvents = 'none';
        }

        // 썸네일+동영상이 동시에 있는 경우는 없음(템플릿에서 분기)

        thumbnailDiv.addEventListener('mouseenter', function () {
            if (!video) return;
            hoverTimer = setTimeout(() => {
                video.style.opacity = '1';
                video.style.pointerEvents = 'auto';
                video.currentTime = 0;
                video.play().then(() => {
                    isPlaying = true;
                    checkTime = setInterval(() => {
                        if (video.currentTime >= 5) {
                            video.pause();
                            video.currentTime = 0;
                            isPlaying = false;
                            // 동영상만 있는 경우: 첫 프레임 유지
                            if (!img) video.currentTime = 0.1;
                            video.style.opacity = '1';
                            video.style.pointerEvents = 'none';
                            clearInterval(checkTime);
                        }
                    }, 100);
                }).catch(err => {
                    isPlaying = false;
                    video.style.opacity = '1';
                    video.style.pointerEvents = 'none';
                });
            }, 1000);
        });

        thumbnailDiv.addEventListener('mouseleave', function () {
            if (!video) return;
            if (hoverTimer) {
                clearTimeout(hoverTimer);
                hoverTimer = null;
            }
            if (isPlaying) {
                video.pause();
                video.currentTime = 0;
                isPlaying = false;
            }
            if (checkTime) {
                clearInterval(checkTime);
                checkTime = null;
            }
            // 동영상만 있는 경우: 첫 프레임 유지
            if (video && !img) {
                video.currentTime = 0.1;
                video.style.opacity = '1';
            }
            video.style.pointerEvents = 'none';
        });
    });
});
