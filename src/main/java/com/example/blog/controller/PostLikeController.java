package com.example.blog.controller;

import com.example.blog.service.PostLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 게시글 좋아요 컨트롤러
 */
/**
 * 게시글 좋아요 컨트롤러.
 *
 * AJAX 요청으로 좋아요 토글과 현재 좋아요 수를 반환합니다.
 */
@Controller
public class PostLikeController {
    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    // 좋아요 토글 (인증 필요) - AJAX 요청 처리
    /**
     * 게시글 좋아요를 토글합니다(인증 필요).
     *
     * @param postId         게시글 식별자
     * @param authentication 인증 정보(사용자 식별)
     * @return `isLiked`, `likeCount`를 포함한 JSON 응답
     */
    @PostMapping("/posts/{postId}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long postId,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> unauthorized = new HashMap<>();
            unauthorized.put("message", "UNAUTHORIZED");
            return ResponseEntity.status(401).body(unauthorized);
        }
        String username = authentication.getName();
        boolean isLiked = postLikeService.toggleLike(postId, username);
        long likeCount = postLikeService.getLikeCount(postId);

        Map<String, Object> response = new HashMap<>();
        response.put("isLiked", isLiked);
        response.put("likeCount", likeCount);

        return ResponseEntity.ok(response);
    }
}
