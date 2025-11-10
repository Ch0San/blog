package com.example.blog.repository;

import com.example.blog.domain.StoryComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 스토리 댓글 리포지토리
 */
/**
 * 스토리 댓글 리포지토리.
 */
@Repository
public interface StoryCommentRepository extends JpaRepository<StoryComment, Long> {
    /**
     * 특정 스토리의 댓글 목록 조회 (삭제되지 않은 것만, 최신순)
     */
    List<StoryComment> findByStoryIdAndIsDeletedFalseOrderByCreatedAtDesc(Long storyId);

    /**
     * 특정 스토리의 댓글 수 (삭제되지 않은 것만)
     */
    long countByStoryIdAndIsDeletedFalse(Long storyId);
}
