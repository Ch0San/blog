package com.example.blog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 블로그 댓글 엔티티
 * 게시글에 달린 댓글 정보를 저장하는 테이블
 * 계층형 댓글 구조를 지원함 (대댓글 가능)
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
public class Comment {
    /** 댓글 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 댓글 내용 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 댓글 작성자 */
    @Column(nullable = false)
    private String author; // 표시용 이름(닉네임)

    /** 댓글 작성자 로그인 아이디 (권한 확인용) */
    @Column(name = "author_username")
    private String authorUsername;

    /** 댓글 좋아요 수 */
    @Column(name = "like_count", columnDefinition = "bigint default 0")
    private Long likeCount = 0L;

    /** 댓글 작성일시 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 댓글 수정일시 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 댓글이 속한 게시글 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /** 부모 댓글 (대댓글인 경우) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    /** 댓글 삭제 여부 (소프트 삭제) */
    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;
}