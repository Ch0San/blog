package com.example.blog.controller;

import com.example.blog.service.StoryCommentLikeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 스토리 댓글 좋아요 컨트롤러
 */
/**
 * 스토리 댓글 좋아요 컨트롤러.
 *
 * AJAX 요청으로 스토리 댓글 좋아요 토글과 현재 좋아요 수를 반환합니다.
 */
@Controller
public class StoryCommentLikeController {
    private final StoryCommentLikeService storyCommentLikeService;

    public StoryCommentLikeController(StoryCommentLikeService storyCommentLikeService) {
        this.storyCommentLikeService = storyCommentLikeService;
    }

    // 스토리 댓글 좋아요 토글 (AJAX)
    /**
     * 스토리 댓글 좋아요를 토글합니다(인증 필요).
     *
     * @param commentId 댓글 식별자
     * @param authentication 인증 정보(사용자 식별)
     * @return `isLiked`, `likeCount`를 포함한 JSON 응답
     */
    @PostMapping("/stories/comments/{commentId}/like")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> toggleLike(
            @PathVariable Long commentId,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("error", "UNAUTHORIZED"));
        }
        String username = authentication.getName();
        boolean isLiked = storyCommentLikeService.toggleLike(commentId, username);
        long likeCount = storyCommentLikeService.getLikeCount(commentId);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("isLiked", isLiked, "likeCount", likeCount));
    }
}
