// stories/detail.js - 스토리 좋아요 및 댓글 좋아요 AJAX 처리
document.addEventListener('DOMContentLoaded', function () {
    // CSRF 토큰 가져오기
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    // 스토리 좋아요 버튼
    const storyLikeBtn = document.getElementById('storyLikeBtn');
    if (storyLikeBtn) {
        storyLikeBtn.addEventListener('click', function () {
            const storyId = this.dataset.storyId;

            fetch(`/stories/${storyId}/like`, {
                method: 'POST',
                headers: {
                    [csrfHeader]: csrfToken
                }
            })
                .then(response => {
                    if (!response.ok) throw new Error('Failed to toggle like');
                    return response.json();
                })
                .then(data => {
                    // 좋아요 수 업데이트
                    const likeCountEl = document.getElementById('storyLikeCount');
                    if (likeCountEl) {
                        likeCountEl.textContent = data.likeCount;
                    }

                    // 버튼 텍스트 및 상태 업데이트
                    const btnTextEl = document.getElementById('storyLikeBtnText');
                    if (data.isLiked) {
                        btnTextEl.textContent = '❤️ 좋아요 취소';
                        storyLikeBtn.classList.add('liked');
                        storyLikeBtn.dataset.isLiked = 'true';
                    } else {
                        btnTextEl.textContent = '🤍 좋아요';
                        storyLikeBtn.classList.remove('liked');
                        storyLikeBtn.dataset.isLiked = 'false';
                    }
                })
                .catch(err => {
                    console.error('Story like error:', err);
                    alert('좋아요 처리 중 오류가 발생했습니다.');
                });
        });
    }

    // 댓글 좋아요 버튼 (여러 개)
    const commentLikeBtns = document.querySelectorAll('.btn-story-comment-like');
    commentLikeBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            const commentId = this.dataset.commentId;

            fetch(`/stories/comments/${commentId}/like`, {
                method: 'POST',
                headers: {
                    [csrfHeader]: csrfToken
                }
            })
                .then(response => {
                    if (!response.ok) throw new Error('Failed to toggle comment like');
                    return response.json();
                })
                .then(data => {
                    // 좋아요 수 업데이트
                    const countEl = document.getElementById(`story-comment-like-count-${commentId}`);
                    if (countEl) {
                        countEl.textContent = data.likeCount;
                    }

                    // 버튼 텍스트 및 상태 업데이트
                    const textEl = document.getElementById(`story-comment-like-text-${commentId}`);
                    if (data.isLiked) {
                        textEl.textContent = '좋아요 취소';
                        btn.dataset.isLiked = 'true';
                    } else {
                        textEl.textContent = '좋아요';
                        btn.dataset.isLiked = 'false';
                    }
                })
                .catch(err => {
                    console.error('Comment like error:', err);
                    alert('좋아요 처리 중 오류가 발생했습니다.');
                });
        });
    });
});

// 댓글 수정 기능
function editComment(commentId) {
    const contentDiv = document.getElementById('comment-content-' + commentId);
    const editForm = document.getElementById('comment-edit-form-' + commentId);

    if (contentDiv && editForm) {
        contentDiv.style.display = 'none';
        editForm.style.display = 'block';
    }
}

function cancelEdit(commentId) {
    const contentDiv = document.getElementById('comment-content-' + commentId);
    const editForm = document.getElementById('comment-edit-form-' + commentId);

    if (contentDiv && editForm) {
        contentDiv.style.display = 'block';
        editForm.style.display = 'none';
    }
}
