package com.example.blog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 방문자 수 카운트 엔티티
 */
/**
 * 방문자 카운트 엔티티.
 *
 * 일자별 방문자 집계(count)를 보관합니다.
 */
@Entity
@Table(name = "visitor_counts")
@Getter
@Setter
@NoArgsConstructor
public class VisitorCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 방문 날짜 */
    @Column(name = "visit_date", unique = true, nullable = false)
    private LocalDate visitDate;

    /** 해당 날짜 방문자 수 */
    @Column(name = "count", nullable = false)
    private Long count = 0L;
}
