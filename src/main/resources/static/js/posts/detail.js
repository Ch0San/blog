// DOM이 준비된 후 이벤트 바인딩 (초기 렌더 타이밍 이슈 방지)
document.addEventListener('DOMContentLoaded', () => {
    // 게시글 좋아요 처리
    const likeBtn = document.getElementById('likeBtn');
    if (likeBtn) {
        likeBtn.addEventListener('click', async function () {
            const postId = this.getAttribute('data-post-id');
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
                || document.querySelector('input[name="_csrf"]')?.value;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

            try {
                const response = await fetch(`/posts/${postId}/like`, {
                    method: 'POST',
                    headers: {
                        [csrfHeader]: csrfToken
                    }
                });

                if (response.status === 401) {
                    alert('로그인이 필요합니다.');
                    window.location.href = '/member/signin';
                    return;
                }

                if (response.ok) {
                    const data = await response.json();

                    // 좋아요 수 업데이트
                    const countEl = document.getElementById('likeCount');
                    if (countEl) countEl.textContent = data.likeCount;

                    // 버튼 텍스트 및 상태 업데이트
                    const likeBtnText = document.getElementById('likeBtnText');
                    if (data.isLiked) {
                        if (likeBtnText) likeBtnText.textContent = '❤️ 좋아요 취소';
                        likeBtn.classList.add('liked');
                    } else {
                        if (likeBtnText) likeBtnText.textContent = '🤍 좋아요';
                        likeBtn.classList.remove('liked');
                    }

                    this.setAttribute('data-is-liked', data.isLiked);
                }
            } catch (error) {
                console.error('좋아요 처리 중 오류:', error);
                alert('좋아요 처리에 실패했습니다.');
            }
        });
    }

    // 댓글 좋아요 처리
    document.querySelectorAll('.btn-comment-like').forEach(btn => {
        btn.addEventListener('click', async function () {
            const commentId = this.getAttribute('data-comment-id');
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

            try {
                const response = await fetch(`/comments/${commentId}/like`, {
                    method: 'POST',
                    headers: {
                        [csrfHeader]: csrfToken
                    }
                });

                if (response.status === 401) {
                    alert('로그인이 필요합니다.');
                    window.location.href = '/member/signin';
                    return;
                }

                if (response.ok) {
                    const data = await response.json();

                    // 좋아요 수 업데이트
                    const likeCountSpan = document.getElementById(`comment-like-count-${commentId}`);
                    if (likeCountSpan) {
                        likeCountSpan.textContent = data.likeCount;
                    }

                    // 버튼 텍스트 업데이트
                    const likeText = document.getElementById(`comment-like-text-${commentId}`);
                    if (likeText) {
                        likeText.textContent = data.isLiked ? '좋아요 취소' : '좋아요';
                    }

                    this.setAttribute('data-is-liked', data.isLiked);
                }
            } catch (error) {
                console.error('댓글 좋아요 처리 중 오류:', error);
                alert('댓글 좋아요 처리에 실패했습니다.');
            }
        });
    });
});

// 댓글 수정 폼 표시
function editComment(commentId) {
    const contentDiv = document.getElementById('comment-content-' + commentId);
    const editForm = document.getElementById('comment-edit-form-' + commentId);

    if (contentDiv && editForm) {
        contentDiv.style.display = 'none';
        editForm.style.display = 'block';
    }
}

// 댓글 수정 취소
function cancelEdit(commentId) {
    const contentDiv = document.getElementById('comment-content-' + commentId);
    const editForm = document.getElementById('comment-edit-form-' + commentId);

    if (contentDiv && editForm) {
        contentDiv.style.display = 'block';
        editForm.style.display = 'none';
    }
}

// 현재 URL 복사
function copyCurrentUrl() {
    const url = window.location.href;
    navigator.clipboard.writeText(url)
        .then(() => { alert('링크가 복사되었습니다.'); })
        .catch(() => { prompt('복사에 실패했습니다. 아래 링크를 복사하세요.', url); });
}

// 트위터 공유
function shareToTwitter() {
    const url = encodeURIComponent(window.location.href);
    window.open('https://twitter.com/intent/tweet?url=' + url, '_blank', 'noopener');
}

// 페이스북 공유
function shareToFacebook() {
    const url = encodeURIComponent(window.location.href);
    window.open('https://www.facebook.com/sharer/sharer.php?u=' + url, '_blank', 'noopener');
}

