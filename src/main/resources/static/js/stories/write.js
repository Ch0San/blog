/*
 * stories/write.js
 * - 스토리 작성 화면의 동영상/썸네일 미리보기 및 용량 검증
 */
// stories/write.html 전용: 미리보기 기능
document.addEventListener('DOMContentLoaded', function () {
  const videoFileInput = document.getElementById('videoFile');
  const videoPreview = document.getElementById('videoPreview');
  const thumbnailFileInput = document.getElementById('thumbnailFile');
  const thumbnailPreview = document.getElementById('thumbnailPreview');
  const maxSize = 100 * 1024 * 1024; // 100MB

  // 동영상 파일 선택 시 미리보기/용량 검사
  if (videoFileInput) {
    videoFileInput.addEventListener('change', function () {
      if (videoPreview) videoPreview.innerHTML = '';
      const file = this.files && this.files[0];
      if (!file) return;
      if (file.size > maxSize) {
        alert('동영상 파일 크기가 100MB를 초과합니다.\n현재 파일 크기: ' + (file.size / 1024 / 1024).toFixed(2) + 'MB');
        this.value = '';
        return;
      }
      const previewContainer = document.createElement('div');
      previewContainer.style.cssText = 'border:1px solid #e6e8eb;border-radius:6px;padding:10px;background:#f8f9fa';
      const fileInfo = document.createElement('div');
      fileInfo.innerHTML = `
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px">
          <span style="font-size:14px;color:#555">[VIDEO]</span>
          <div>
            <div style="font-weight:500;color:var(--text)">${file.name}</div>
            <div style="font-size:12px;color:var(--muted)">${(file.size / 1024 / 1024).toFixed(2)} MB</div>
          </div>
        </div>`;
      const video = document.createElement('video');
      video.controls = true;
      video.style.cssText = 'width:100%;max-width:500px;border-radius:6px;display:block';
      video.src = URL.createObjectURL(file);
      previewContainer.appendChild(fileInfo);
      previewContainer.appendChild(video);
      if (videoPreview) videoPreview.appendChild(previewContainer);
    });
  }

  // 썸네일 파일 선택 시 미리보기/용량 검사
  if (thumbnailFileInput) {
    thumbnailFileInput.addEventListener('change', function () {
      if (thumbnailPreview) thumbnailPreview.innerHTML = '';
      const file = this.files && this.files[0];
      if (!file) return;
      if (file.size > 10 * 1024 * 1024) {
        alert('썸네일 파일 크기가 10MB를 초과합니다.\n현재 파일 크기: ' + (file.size / 1024 / 1024).toFixed(2) + 'MB');
        this.value = '';
        return;
      }
      const previewContainer = document.createElement('div');
      previewContainer.style.cssText = 'border:1px solid #e6e8eb;border-radius:6px;padding:10px;background:#f8f9fa';
      const fileInfo = document.createElement('div');
      fileInfo.innerHTML = `
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px">
          <span style="font-size:14px;color:#555">[IMAGE]</span>
          <div>
            <div style="font-weight:500;color:var(--text)">${file.name}</div>
            <div style="font-size:12px;color:var(--muted)">${(file.size / 1024 / 1024).toFixed(2)} MB</div>
          </div>
        </div>`;
      const img = document.createElement('img');
      img.style.cssText = 'width:100%;max-width:400px;border-radius:6px;display:block';
      img.src = URL.createObjectURL(file);
      previewContainer.appendChild(fileInfo);
      previewContainer.appendChild(img);
      if (thumbnailPreview) thumbnailPreview.appendChild(previewContainer);
    });
  }
});
