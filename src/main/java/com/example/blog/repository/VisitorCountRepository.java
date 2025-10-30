package com.example.blog.repository;

import com.example.blog.domain.VisitorCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface VisitorCountRepository extends JpaRepository<VisitorCount, Long> {

    // 특정 날짜의 방문자 수 조회
    Optional<VisitorCount> findByVisitDate(LocalDate visitDate);

    // 전체 방문자 수 합계
    @Query("SELECT SUM(v.count) FROM VisitorCount v")
    Long getTotalVisitorCount();
}
