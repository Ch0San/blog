package com.example.blog.repository;

import com.example.blog.domain.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

/**
 * Story 리포지토리.
 */
public interface StoryRepository extends JpaRepository<Story, Long> {
    // 카테고리별 스토리 조회
    Page<Story> findByCategory(String category, Pageable pageable);

    // 공개된 스토리만 조회
    Page<Story> findByIsPublicTrue(Pageable pageable);

    // 카테고리별 공개된 스토리 조회
    Page<Story> findByCategoryAndIsPublicTrue(String category, Pageable pageable);

    // 제목 포함 검색 (공개만)
    Page<Story> findByTitleContainingIgnoreCaseAndIsPublicTrue(String title, Pageable pageable);

    // 태그 포함 검색 (공개만)
    Page<Story> findByTagsContainingIgnoreCaseAndIsPublicTrue(String tag, Pageable pageable);

    // 설명(내용) 포함 검색 (공개만)
    Page<Story> findByDescriptionContainingIgnoreCaseAndIsPublicTrue(String description, Pageable pageable);

    // 날짜(하루 범위) 검색 (공개만)
    Page<Story> findByCreatedAtBetweenAndIsPublicTrue(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
