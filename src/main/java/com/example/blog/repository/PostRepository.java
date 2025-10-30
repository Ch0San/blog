package com.example.blog.repository;

import com.example.blog.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Post JPA Repository
 */
public interface PostRepository extends JpaRepository<Post, Long> {
    // 카테고리별 게시글 조회
    Page<Post> findByCategory(String category, Pageable pageable);

    // 제목 포함 검색
    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 태그 포함 검색
    Page<Post> findByTagsContainingIgnoreCase(String tag, Pageable pageable);

    // 내용 포함 검색
    Page<Post> findByContentContainingIgnoreCase(String content, Pageable pageable);

    // 날짜(하루 범위) 검색
    Page<Post> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // images를 함께 fetch join으로 조회
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Post> findByIdWithImages(@Param("id") Long id);
}
