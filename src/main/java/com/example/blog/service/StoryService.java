package com.example.blog.service;

import com.example.blog.domain.Story;
import com.example.blog.repository.StoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 스토리(Shorts) 서비스
 */
/**
 * 스토리(Shorts) 서비스.
 *
 * 스토리의 조회/검색/저장/삭제 및 인기/최신 목록을 제공합니다.
 * 로컬 `/uploads/...` 경로에 저장된 첨부 리소스 정리 로직을 포함합니다.
 */
@Service
public class StoryService {
    private final StoryRepository storyRepository;

    public StoryService(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    // 전체 스토리 조회 (공개된 것만)
    /**
     * 전체 스토리 페이지를 조회합니다.
     *
     * @param page 0부터 시작하는 페이지 번호
     * @param size 페이지 크기
     * @return 스토리 페이지(최신 생성일 내림차순)
     */
    public Page<Story> getStories(int page, int size) {
        // 임시: 모든 스토리 조회 (공개 여부 무시)
        return storyRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 카테고리별 스토리 조회 (공개된 것만)
    /**
     * 카테고리별 스토리 페이지를 조회합니다.
     *
     * @param category 카테고리명
     * @param page     0부터 시작하는 페이지 번호
     * @param size     페이지 크기
     * @return 스토리 페이지(최신 생성일 내림차순)
     */
    public Page<Story> getStoriesByCategory(String category, int page, int size) {
        // 임시: 모든 스토리 조회 (공개 여부 무시)
        return storyRepository.findByCategory(
                category,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 스토리 ID로 조회
    /**
     * 스토리를 ID로 조회합니다.
     *
     * @param id 스토리 식별자
     * @return 스토리 또는 null
     */
    public Story getStoryById(Long id) {
        return storyRepository.findById(id).orElse(null);
    }

    // 조회수 증가와 함께 스토리 조회
    /**
     * 스토리를 조회하고 조회수를 1 증가시킵니다.
     *
     * @param id 스토리 식별자
     * @return 스토리 또는 null
     */
    @Transactional
    public Story getStoryByIdAndIncrementViewCount(Long id) {
        Story story = storyRepository.findById(id).orElse(null);
        if (story != null) {
            story.setViewCount(story.getViewCount() + 1);
            storyRepository.save(story);
        }
        return story;
    }

    // 인기 스토리 조회 (조회수 순, 공개된 것만)
    /**
     * 인기 스토리를 조회합니다(조회수 내림차순, 상위 N개).
     *
     * @param limit 개수
     * @return 스토리 목록
     */
    public List<Story> getPopularStories(int limit) {
        return storyRepository.findByIsPublicTrue(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "viewCount"))).getContent();
    }

    // 최신 스토리 조회 (작성일 순, 공개된 것만)
    /**
     * 최신 스토리를 조회합니다(생성일 내림차순, 상위 N개).
     *
     * @param limit 개수
     * @return 스토리 목록
     */
    public List<Story> getRecentStories(int limit) {
        return storyRepository.findByIsPublicTrue(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }

    /**
     * 스토리를 저장합니다.
     *
     * @param story 스토리 엔티티
     * @return 저장된 스토리
     */
    @Transactional
    public Story saveStory(Story story) {
        return storyRepository.save(story);
    }

    // 검색: 제목 (공개)
    /**
     * 제목으로 스토리를 검색합니다(대소문자 무시, 부분일치, 공개만).
     */
    public Page<Story> searchByTitle(String keyword, int page, int size) {
        return storyRepository.findByTitleContainingIgnoreCaseAndIsPublicTrue(
                keyword, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 검색: 태그 (공개)
    /**
     * 태그로 스토리를 검색합니다(대소문자 무시, 부분일치, 공개만).
     */
    public Page<Story> searchByTags(String keyword, int page, int size) {
        return storyRepository.findByTagsContainingIgnoreCaseAndIsPublicTrue(
                keyword, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 검색: 내용(설명) (공개)
    /**
     * 설명/본문으로 스토리를 검색합니다(대소문자 무시, 부분일치, 공개만).
     */
    public Page<Story> searchByContent(String keyword, int page, int size) {
        return storyRepository.findByDescriptionContainingIgnoreCaseAndIsPublicTrue(
                keyword, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 검색: 작성일(하루) (공개)
    /**
     * 특정 날짜 생성 스토리를 검색합니다(자정~23:59:59.999, 공개만).
     */
    public Page<Story> searchByDate(java.time.LocalDate date, int page, int size) {
        java.time.LocalDateTime start = date.atStartOfDay();
        java.time.LocalDateTime end = date.atTime(23, 59, 59, 999000000);
        return storyRepository.findByCreatedAtBetweenAndIsPublicTrue(
                start, end, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 검색: 작성일 범위 (공개)
    /**
     * 날짜 범위로 스토리를 검색합니다(공개만).
     */
    public Page<Story> searchByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate, int page,
            int size) {
        if (startDate == null && endDate == null) {
            startDate = java.time.LocalDate.now();
            endDate = startDate;
        } else if (startDate == null) {
            startDate = endDate;
        } else if (endDate == null) {
            endDate = startDate;
        }
        java.time.LocalDateTime start = startDate.atStartOfDay();
        java.time.LocalDateTime end = endDate.atTime(23, 59, 59, 999000000);
        return storyRepository.findByCreatedAtBetweenAndIsPublicTrue(
                start, end, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 스토리 삭제 (+ 로컬 파일 정리)
    /**
     * 스토리를 삭제합니다. 로컬 업로드 경로의 영상/썸네일 파일을 함께 정리합니다.
     *
     * @param id 스토리 식별자
     */
    @Transactional
    public void deleteStory(Long id) {
        Story story = storyRepository.findById(id).orElse(null);
        if (story != null) {
            // 영상 파일과 썸네일이 로컬 업로드 경로라면 삭제
            deleteLocalFileIfUnderUploads(story.getVideoUrl());
            deleteLocalFileIfUnderUploads(story.getThumbnailUrl());
        }
        storyRepository.deleteById(id);
    }

    private void deleteLocalFileIfUnderUploads(String url) {
        if (url == null || url.isBlank())
            return;
        String normalizedUrl = url.trim();
        if (!(normalizedUrl.startsWith("/uploads/") || normalizedUrl.startsWith("uploads/"))) {
            return;
        }
        try {
            Path uploadsRoot = Paths.get("uploads").toAbsolutePath().normalize();
            String relative = normalizedUrl.startsWith("/") ? normalizedUrl.substring(1) : normalizedUrl;
            Path target = uploadsRoot.resolve(relative.substring("uploads/".length())).normalize();
            if (!target.startsWith(uploadsRoot))
                return;
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
        }
    }

    // 전체 스토리 수
    /**
     * 전체 스토리 수를 조회합니다.
     *
     * @return 총 개수
     */
    public long getTotalStoryCount() {
        return storyRepository.count();
    }
}
