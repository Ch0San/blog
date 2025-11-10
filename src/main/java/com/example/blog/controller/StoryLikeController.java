package com.example.blog.controller;

import com.example.blog.service.StoryLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * 스토리 좋아요 컨트롤러.
 *
 * AJAX 요청으로 좋아요 토글과 현재 좋아요 수를 반환합니다.
 */
@Controller
public class StoryLikeController {
    private final StoryLikeService storyLikeService;

    public StoryLikeController(StoryLikeService storyLikeService) {
        this.storyLikeService = storyLikeService;
    }

    /**
     * 스토리 좋아요를 토글합니다(인증 필요).
     *
     * @param storyId        스토리 식별자
     * @param authentication 인증 정보(사용자 식별)
     * @return `isLiked`, `likeCount`를 포함한 JSON 응답
     */
    @PostMapping("/stories/{storyId}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long storyId,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        }
        String username = authentication.getName();
        boolean isLiked = storyLikeService.toggleLike(storyId, username);
        long likeCount = storyLikeService.getLikeCount(storyId);
        return ResponseEntity.ok(Map.of("isLiked", isLiked, "likeCount", likeCount));
    }
}
