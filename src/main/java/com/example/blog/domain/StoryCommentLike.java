package com.example.blog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 스토리 댓글 좋아요 엔티티
 * 사용자와 스토리 댓글 간의 좋아요 관계를 저장합니다.
 */
/**
 * 스토리 댓글 좋아요 엔티티.
 *
 * 사용자-스토리댓글 간 좋아요 관계를 저장합니다(복합 유니크 제약).
 */
@Entity
@Table(name = "story_comment_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "comment_id", "username" })
})
@Getter
@Setter
@NoArgsConstructor
public class StoryCommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private StoryComment comment;

    @Column(nullable = false)
    private String username;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
