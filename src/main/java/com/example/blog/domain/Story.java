package com.example.blog.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Story(Shorts) 엔티티
 * 동영상 기반의 짧은 콘텐츠
 */
/**
 * 스토리(Shorts) 엔티티.
 *
 * 영상 기반의 짧은 콘텐츠 정보와 공개/통계 필드를 보관합니다.
 */
@Entity
@Table(name = "stories")
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title; // 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 설명

    @Column(nullable = false, length = 100)
    private String author; // 작성자

    @Column(nullable = false, length = 500)
    private String videoUrl; // 동영상 URL (필수)

    @Column(length = 500)
    private String thumbnailUrl; // 썸네일 이미지 URL

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount = 0L; // 조회수

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long likeCount = 0L; // 좋아요 수

    @Column(length = 50)
    private String category; // 카테고리 (예: 여행, 맛집, 기술, 일상, 브이로그)

    @Column(length = 500)
    private String tags; // 태그 (쉼표로 구분)

    @Column(nullable = false)
    private Integer duration = 0; // 동영상 길이(초)

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean isPublic = true; // 공개 여부

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
