package com.example.blog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 블로그 게시글 엔티티
 * 게시글의 기본 정보와 메타데이터를 저장하는 테이블
 */
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post {
    /** 게시글 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 게시글 제목 */
    @Column(nullable = false)
    private String title;

    /** 게시글 본문 내용 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 작성자 이름 */
    @Column(nullable = false)
    private String author;

    /** 조회수 */
    @Column(name = "view_count", columnDefinition = "bigint default 0")
    private Long viewCount = 0L;

    /** 좋아요 수 */
    @Column(name = "like_count", columnDefinition = "bigint default 0")
    private Long likeCount = 0L;

    /** 게시글 공개 여부 */
    @Column(name = "is_public", columnDefinition = "boolean default true")
    private boolean isPublic = true;

    /** 썸네일 이미지 URL */
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    /** 동영상 URL (예: YouTube 링크 등) */
    @Column(name = "video_url")
    private String videoUrl;

    /** 게시글 카테고리 */
    @Column(name = "category")
    private String category;

    /** 게시글 태그 (쉼표로 구분) */
    @Column(name = "tags")
    private String tags;

    /** 게시글 작성일시 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 게시글 수정일시 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 게시글에 달린 댓글 목록 */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    /** 게시글에 첨부된 이미지 목록 (정렬 순서 오름차순) */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<PostImage> images = new ArrayList<>();
}