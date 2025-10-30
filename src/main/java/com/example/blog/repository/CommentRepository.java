package com.example.blog.repository;

import com.example.blog.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Comment JPA Repository
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 게시글별 댓글 조회 (삭제되지 않은 것만)
    List<Comment> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(Long postId);
}
