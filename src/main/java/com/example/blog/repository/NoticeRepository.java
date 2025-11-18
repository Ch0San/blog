package com.example.blog.repository;

import com.example.blog.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Notice 리포지토리.
 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    /**
     * created_at / updated_at 중 더 최신 값 기준 역순 목록
     */
    @Query("SELECT n FROM Notice n ORDER BY COALESCE(n.updatedAt, n.createdAt) DESC")
    Page<Notice> findAllByLatestDateDesc(Pageable pageable);
}
