/*\n * stories/write.js\n * - 스토리 작성 화면의 동영상/썸네일 파일 미리보기 및 용량 검증\n */\n// stories/write.html???숈쁺???몃꽕??誘몃━蹂닿린 湲곕뒫
document.addEventListener('DOMContentLoaded', function () {
    const videoFileInput = document.getElementById('videoFile');
    const videoPreview = document.getElementById('videoPreview');
    const thumbnailFileInput = document.getElementById('thumbnailFile');
    const thumbnailPreview = document.getElementById('thumbnailPreview');
    const maxSize = 100 * 1024 * 1024; // 100MB

    if (videoFileInput) {
        videoFileInput.addEventListener('change', function () {
            videoPreview.innerHTML = '';
            const file = this.files[0];
            if (!file) return;
            // ?뚯씪 ?ш린 泥댄겕
            if (file.size > maxSize) {
                alert('?숈쁺???뚯씪 ?ш린媛 100MB瑜?珥덇낵?⑸땲??\n현재 ?뚯씪 ?ш린: ' + (file.size / 1024 / 1024).toFixed(2) + 'MB');
                this.value = '';
                return;
            }
            // 誘몃━蹂닿린 ?쒖떆
            const previewContainer = document.createElement('div');
            previewContainer.style.cssText = 'border:1px solid #e6e8eb;border-radius:6px;padding:10px;background:#f8f9fa';
            const fileInfo = document.createElement('div');
            fileInfo.innerHTML = `
				<div style="display:flex;align-items:center;gap:8px;margin-bottom:8px">
					<span style="font-size:20px">?렗</span>
					<div>
						<div style="font-weight:500;color:var(--text)">${file.name}</div>
						<div style="font-size:12px;color:var(--muted)">${(file.size / 1024 / 1024).toFixed(2)} MB</div>
					</div>
				</div>
			`;
            // ?숈쁺??誘몃━蹂닿린 (URL.createObjectURL ?ъ슜)
            const video = document.createElement('video');
            video.controls = true;
            video.style.cssText = 'width:100%;max-width:500px;border-radius:6px';
            video.src = URL.createObjectURL(file);
            previewContainer.appendChild(fileInfo);
            previewContainer.appendChild(video);
            videoPreview.appendChild(previewContainer);
        });
    }

    if (thumbnailFileInput) {
        thumbnailFileInput.addEventListener('change', function () {
            thumbnailPreview.innerHTML = '';
            const file = this.files[0];
            if (!file) return;
            // ?뚯씪 ?ш린 泥댄겕 (10MB)
            if (file.size > 10 * 1024 * 1024) {
                alert('?대?吏 ?뚯씪 ?ш린媛 10MB瑜?珥덇낵?⑸땲??\n현재 ?뚯씪 ?ш린: ' + (file.size / 1024 / 1024).toFixed(2) + 'MB');
                this.value = '';
                return;
            }
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
});


