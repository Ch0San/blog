package com.example.blog.controller;

import com.example.blog.service.CommentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 댓글 컨트롤러
 */
/**
 * 댓글 컨트롤러.
 *
 * 게시글에 대한 댓글 생성/수정/삭제를 처리합니다.
 */
@Controller
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 댓글 작성 (인증 필요)
    /**
     * 댓글 생성(인증 필요).
     *
     * @param postId  게시글 식별자
     * @param content 댓글 내용
     * @param authentication 인증 정보(작성자 식별)
     * @return 게시글 상세로 리다이렉트
     */
    @PostMapping("/posts/{postId}/comments")
    public String createComment(
            @PathVariable Long postId,
            @RequestParam String content,
            Authentication authentication) {
        String username = authentication.getName();
        commentService.createComment(postId, content, username);
        return "redirect:/posts/" + postId;
    }

    // 댓글 삭제 (인증 필요)
    /**
     * 댓글 삭제(인증 필요).
     *
     * @param commentId 댓글 식별자
     * @param postId    소속 게시글 식별자
     * @return 게시글 상세로 리다이렉트
     */
    @PostMapping("/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long postId) {
        commentService.deleteComment(commentId);
        return "redirect:/posts/" + postId;
    }

    // 댓글 수정 (인증 필요)
    /**
     * 댓글 수정(인증 필요). 작성자 본인만 수정할 수 있습니다.
     *
     * @param commentId 댓글 식별자
     * @param postId    소속 게시글 식별자
     * @param content   변경할 내용
     * @param authentication 인증 정보(작성자 확인)
     * @return 게시글 상세로 리다이렉트. 실패 시 오류 메시지 쿼리 포함
     */
    @PostMapping("/comments/{commentId}/update")
    public String updateComment(
            @PathVariable Long commentId,
            @RequestParam Long postId,
            @RequestParam String content,
            Authentication authentication) {
        String username = authentication.getName();
        try {
            commentService.updateComment(commentId, content, username);
        } catch (IllegalArgumentException e) {
            // 권한 없음 또는 댓글 없음 - 에러 처리는 추후 개선 가능
            return "redirect:/posts/" + postId + "?error=" + e.getMessage();
        }
        return "redirect:/posts/" + postId;
    }
}
