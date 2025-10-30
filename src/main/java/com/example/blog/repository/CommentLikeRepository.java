package com.example.blog.repository;

import com.example.blog.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * CommentLike JPA Repository
 */
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentIdAndUsername(Long commentId, String username);

    boolean existsByCommentIdAndUsername(Long commentId, String username);

    long countByCommentId(Long commentId);

    // 특정 게시글(post)의 댓글들 중, 사용자가 좋아요한 목록 조회
    List<CommentLike> findAllByComment_Post_IdAndUsername(Long postId, String username);
}
