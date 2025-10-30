package com.example.blog.service;

import com.example.blog.domain.Comment;
import com.example.blog.domain.CommentLike;
import com.example.blog.repository.CommentLikeRepository;
import com.example.blog.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 댓글 좋아요 서비스
 */
@Service
public class CommentLikeService {
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

    public CommentLikeService(CommentLikeRepository commentLikeRepository, CommentRepository commentRepository) {
        this.commentLikeRepository = commentLikeRepository;
        this.commentRepository = commentRepository;
    }

    // 좋아요 토글 (있으면 취소, 없으면 추가)
    @Transactional
    public boolean toggleLike(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        var existing = commentLikeRepository.findByCommentIdAndUsername(commentId, username);
        if (existing.isPresent()) {
            commentLikeRepository.delete(existing.get());
            // 방어 로직: 음수 방지
            long newCount = Math.max(0, (comment.getLikeCount() == null ? 0 : comment.getLikeCount().intValue()) - 1);
            comment.setLikeCount((long) newCount);
            commentRepository.save(comment);
            return false;
        } else {
            CommentLike like = new CommentLike();
            like.setComment(comment);
            like.setUsername(username);
            commentLikeRepository.save(like);

            long newCount = (comment.getLikeCount() == null ? 0 : comment.getLikeCount()) + 1;
            comment.setLikeCount(newCount);
            commentRepository.save(comment);
            return true;
        }
    }

    public boolean isLiked(Long commentId, String username) {
        return commentLikeRepository.existsByCommentIdAndUsername(commentId, username);
    }

    // 게시글 내에서 사용자가 좋아요한 댓글 ID 집합
    public Set<Long> getLikedCommentIdsForPost(Long postId, String username) {
        return commentLikeRepository.findAllByComment_Post_IdAndUsername(postId, username)
                .stream()
                .map(cl -> cl.getComment().getId())
                .collect(Collectors.toSet());
    }

    // 댓글 좋아요 수 조회
    public long getLikeCount(Long commentId) {
        return commentLikeRepository.countByCommentId(commentId);
    }
}
