package com.example.blog.controller;

import com.example.blog.service.StoryLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class StoryLikeController {
    private final StoryLikeService storyLikeService;

    public StoryLikeController(StoryLikeService storyLikeService) {
        this.storyLikeService = storyLikeService;
    }

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
