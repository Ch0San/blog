package com.example.blog.repository;

import com.example.blog.domain.StoryCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * StoryCommentLike JPA Repository
 */
public interface StoryCommentLikeRepository extends JpaRepository<StoryCommentLike, Long> {
    Optional<StoryCommentLike> findByCommentIdAndUsername(Long commentId, String username);

    boolean existsByCommentIdAndUsername(Long commentId, String username);

    long countByCommentId(Long commentId);

    // 특정 스토리의 댓글들 중, 사용자가 좋아요한 목록 조회
    List<StoryCommentLike> findAllByComment_Story_IdAndUsername(Long storyId, String username);
}
