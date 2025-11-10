package com.example.blog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 스토리 좋아요 엔티티
 */
/**
 * 스토리 좋아요 엔티티.
 *
 * 사용자-스토리 간 좋아요 관계를 저장합니다(복합 유니크 제약).
 */
@Entity
@Table(name = "story_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "story_id", "username" })
})
@Getter
@Setter
@NoArgsConstructor
public class StoryLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(nullable = false)
    private String username;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
