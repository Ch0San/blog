package com.example.blog.repository;

import com.example.blog.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Notice 리포지토리.
 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    /** 생성일 내림차순 전체 목록 페이징 */
    Page<Notice> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
