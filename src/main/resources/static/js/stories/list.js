/*
 * stories/list.js
 * - 목록 카드에서 썸네일/비디오 hover 미리보기 전환
 * - 마우스 오버 지연(1s) 후 재생, 최대 5초까지만 재생/복구
 */
document.addEventListener('DOMContentLoaded', function () {
  const storyThumbnails = document.querySelectorAll('.stories-thumbnail');

  storyThumbnails.forEach((thumbnailDiv) => {
    /** @type {HTMLVideoElement|null} */
    const video = thumbnailDiv.querySelector('.video-thumbnail');
    /** @type {HTMLImageElement|null} */
    const img = thumbnailDiv.querySelector('.thumbnail-img');

    let hoverTimer = null;
    let checkTime = null;
    let playPromise = null;
    let isPlaying = false;

    // 비디오가 없으면(이미지 카드) 추가 동작 불필요
    if (!video) return;

    // 초기 상태 설정
    // - 이미지가 있으면 이미지를 우선 표시(img opacity 1 / video opacity 0)
    // - 비디오만 있으면 비디오의 첫 프레임(0.1s)을 썸네일처럼 보여주기
    function showFirstFrameAsThumb() {
      try {
        video.currentTime = 0.1; // 첫 프레임 근처
      } catch (_) {}
      video.pause();
      isPlaying = false;

      if (img) {
        video.style.opacity = '0';
        video.style.pointerEvents = 'none';
        img.style.opacity = '1';
        if (!img.style.transition) img.style.transition = 'opacity .15s';
      } else {
        video.style.opacity = '1';
        video.style.pointerEvents = 'none';
      }
    }

    if (video.readyState >= 1) {
      showFirstFrameAsThumb();
    } else {
      video.addEventListener('loadedmetadata', showFirstFrameAsThumb, { once: true });
    }

    // hover 시작: 1초 후 미리보기 재생
    thumbnailDiv.addEventListener('mouseenter', function () {
      if (hoverTimer) return; // 중복 방지
      hoverTimer = setTimeout(() => {
        if (img) {
          img.style.opacity = '0';
          img.style.transition = 'opacity .15s';
        }
        video.style.opacity = '1';
        video.style.pointerEvents = 'auto';
        video.currentTime = 0;

        playPromise = video
          .play()
          .then(() => {
            isPlaying = true;
            // 최대 5초만 재생
            checkTime = setInterval(() => {
              if (video.currentTime >= 5) {
                stopPreviewAndRestore();
              }
            }, 100);
          })
          .catch(() => {
            // 자동 재생이 차단된 경우 복구
            isPlaying = false;
            restoreStillFrame();
          });
      }, 1000);
    });

    // hover 종료: 즉시 복구
    thumbnailDiv.addEventListener('mouseleave', function () {
      if (hoverTimer) {
        clearTimeout(hoverTimer);
        hoverTimer = null;
      }

      if (playPromise) {
        // play()는 비동기이므로 then 이후에 pause
        playPromise.then(() => {
          stopPreviewAndRestore();
        });
        playPromise = null;
      } else if (isPlaying) {
        stopPreviewAndRestore();
      } else {
        restoreStillFrame();
      }
    });

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

    function restoreStillFrame() {
      try {
        video.currentTime = 0.1;
      } catch (_) {}
      video.pause();
      video.style.pointerEvents = 'none';

      if (img) {
        img.style.opacity = '1';
        video.style.opacity = '0';
      } else {
        video.style.opacity = '1';
      }
    }
  });
});
