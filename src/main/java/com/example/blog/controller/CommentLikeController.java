package com.example.blog.controller;

import com.example.blog.service.CommentLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 댓글 좋아요 컨트롤러
 */
@Controller
public class CommentLikeController {
    private final CommentLikeService commentLikeService;

    public CommentLikeController(CommentLikeService commentLikeService) {
        this.commentLikeService = commentLikeService;
    }

    // 댓글 좋아요 토글 (인증 필요) - AJAX 요청 처리
    @PostMapping("/comments/{commentId}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long commentId,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> unauthorized = new HashMap<>();
            unauthorized.put("message", "UNAUTHORIZED");
            return ResponseEntity.status(401).body(unauthorized);
        }
        String username = authentication.getName();
        boolean isLiked = commentLikeService.toggleLike(commentId, username);
        long likeCount = commentLikeService.getLikeCount(commentId);

        Map<String, Object> response = new HashMap<>();
        response.put("isLiked", isLiked);
        response.put("likeCount", likeCount);

        return ResponseEntity.ok(response);
    }
}
