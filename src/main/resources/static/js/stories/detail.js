/*\n * stories/detail.js\n * - 스토리 좋아요/댓글 좋아요 토글 및 카운트 업데이트\n * - CSRF 메타 태그 사용\n */\n?ㅽ넗由?醫뗭븘??諛??볤? 醫뗭븘??AJAX 泥섎━
document.addEventListener('DOMContentLoaded', function () {
    // CSRF ?좏겙 媛?몄삤湲?
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    // ?ㅽ넗由?醫뗭븘??踰꾪듉
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
                    // 醫뗭븘?????낅뜲?댄듃
                    const likeCountEl = document.getElementById('storyLikeCount');
                    if (likeCountEl) {
                        likeCountEl.textContent = data.likeCount;
                    }

                    // 踰꾪듉 ?띿뒪??諛??곹깭 ?낅뜲?댄듃
                    const btnTextEl = document.getElementById('storyLikeBtnText');
                    if (data.isLiked) {
                        btnTextEl.textContent = '?ㅿ툘 醫뗭븘??痍⑥냼';
                        storyLikeBtn.classList.add('liked');
                        storyLikeBtn.dataset.isLiked = 'true';
                    } else {
                        btnTextEl.textContent = '?쨳 醫뗭븘??;
                        storyLikeBtn.classList.remove('liked');
                        storyLikeBtn.dataset.isLiked = 'false';
                    }
                })
                .catch(err => {
                    console.error('Story like error:', err);
                    alert('醫뗭븘??泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.');
                });
        });
    }

    // ?볤? 醫뗭븘??踰꾪듉 (?щ윭 媛?
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
                    // 醫뗭븘?????낅뜲?댄듃
                    const countEl = document.getElementById(`story-comment-like-count-${commentId}`);
                    if (countEl) {
                        countEl.textContent = data.likeCount;
                    }

                    // 踰꾪듉 ?띿뒪??諛??곹깭 ?낅뜲?댄듃
                    const textEl = document.getElementById(`story-comment-like-text-${commentId}`);
                    if (data.isLiked) {
                        textEl.textContent = '醫뗭븘??痍⑥냼';
                        btn.dataset.isLiked = 'true';
                    } else {
                        textEl.textContent = '醫뗭븘??;
                        btn.dataset.isLiked = 'false';
                    }
                })
                .catch(err => {
                    console.error('Comment like error:', err);
                    alert('醫뗭븘??泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.');
                });
        });
    });
});

// ?볤? ?섏젙 湲곕뒫
/**\n * 댓글 수정 폼 열기\n * @param {number|string} commentId 댓글 ID\n */\nfunction editComment(commentId) {
    const contentDiv = document.getElementById('comment-content-' + commentId);
    const editForm = document.getElementById('comment-edit-form-' + commentId);

    if (contentDiv && editForm) {
        contentDiv.style.display = 'none';
        editForm.style.display = 'block';
    }
}

/**\n * 댓글 수정 폼 닫기\n * @param {number|string} commentId 댓글 ID\n */\nfunction cancelEdit(commentId) {
    const contentDiv = document.getElementById('comment-content-' + commentId);
    const editForm = document.getElementById('comment-edit-form-' + commentId);

    if (contentDiv && editForm) {
        contentDiv.style.display = 'block';
        editForm.style.display = 'none';
    }
}




