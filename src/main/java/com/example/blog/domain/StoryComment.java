package com.example.blog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 스토리 댓글 엔티티
 * 스토리에 달린 댓글 정보를 저장하는 테이블
 */
/**
 * 스토리 댓글 엔티티.
 *
 * 스토리에 달린 댓글의 내용/작성자/상태를 보관합니다.
 */
@Entity
@Table(name = "story_comments")
@Getter
@Setter
@NoArgsConstructor
public class StoryComment {
    /** 댓글 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 댓글 내용 */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 댓글 작성자 닉네임 (표시용) */
    @Column(nullable = false)
    private String author;

    /** 댓글 작성자 로그인 아이디 (권한 확인용) */
    @Column(name = "author_username", nullable = false)
    private String authorUsername;

    /** 댓글 작성일시 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 댓글 수정일시 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 댓글이 속한 스토리 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    /** 댓글 좋아요 수 */
    @Column(name = "like_count", columnDefinition = "bigint default 0")
    private Long likeCount = 0L;

    /** 댓글 삭제 여부 (소프트 삭제) */
    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;
}
