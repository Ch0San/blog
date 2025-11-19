package com.example.blog.controller;

import com.example.blog.service.CommentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Comment controller.
 * Handles create/update/delete for post comments.
 */
@Controller
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Create comment (auth required).
     */
    @PostMapping("/posts/{postId}/comments")
    public String createComment(
            @PathVariable Long postId,
            @RequestParam String content,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        try {
            commentService.createComment(postId, content, username);
            return "redirect:/posts/" + postId + "?success=\uB313\uAE00\uC774 \uB4F1\uB85D\uB418\uC5C8\uC2B5\uB2C8\uB2E4.";
        } catch (IllegalArgumentException e) {
            return "redirect:/posts/" + postId + "?error=" + e.getMessage();
        } catch (Exception e) {
            return "redirect:/posts/" + postId + "?error=\uB313\uAE00 \uC791\uC131 \uC911 \uC624\uB958\uAC00 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4.";
        }
    }

    /**
     * Delete comment (auth required).
     */
    @PostMapping("/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long postId) {
        commentService.deleteComment(commentId);
        return "redirect:/posts/" + postId + "?success=\uB313\uAE00\uC774 \uC0AD\uC81C\uB418\uC5C8\uC2B5\uB2C8\uB2E4.";
    }

    /**
     * Update comment (auth required). Only author can update.
     */
    @PostMapping("/comments/{commentId}/update")
    public String updateComment(
            @PathVariable Long commentId,
            @RequestParam Long postId,
            @RequestParam String content,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        try {
            commentService.updateComment(commentId, content, username);
        } catch (IllegalArgumentException e) {
            return "redirect:/posts/" + postId + "?error=" + e.getMessage();
        }
        return "redirect:/posts/" + postId + "?success=\uB313\uAE00\uC774 \uC218\uC815\uB418\uC5C8\uC2B5\uB2C8\uB2E4.";
    }
}
