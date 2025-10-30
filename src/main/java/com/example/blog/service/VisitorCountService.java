package com.example.blog.service;

import com.example.blog.domain.VisitorCount;
import com.example.blog.repository.VisitorCountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 방문자 수 카운트 서비스
 */
@Service
public class VisitorCountService {

    private final VisitorCountRepository visitorCountRepository;

    public VisitorCountService(VisitorCountRepository visitorCountRepository) {
        this.visitorCountRepository = visitorCountRepository;
    }

    /**
     * 오늘 방문자 수 증가
     */
    @Transactional
    public void incrementTodayVisitor() {
        LocalDate today = LocalDate.now();
        VisitorCount visitorCount = visitorCountRepository.findByVisitDate(today)
                .orElseGet(() -> {
                    VisitorCount newCount = new VisitorCount();
                    newCount.setVisitDate(today);
                    newCount.setCount(0L);
                    return newCount;
                });

        visitorCount.setCount(visitorCount.getCount() + 1);
        visitorCountRepository.save(visitorCount);
    }

    /**
     * 오늘 방문자 수 조회
     */
    public Long getTodayVisitorCount() {
        LocalDate today = LocalDate.now();
        return visitorCountRepository.findByVisitDate(today)
                .map(VisitorCount::getCount)
                .orElse(0L);
    }

    /**
     * 전체 방문자 수 조회
     */
    public Long getTotalVisitorCount() {
        Long total = visitorCountRepository.getTotalVisitorCount();
        return total != null ? total : 0L;
    }
}
