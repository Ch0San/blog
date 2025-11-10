package com.example.blog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 게시글 다중 이미지 엔티티
 */
/**
 * 게시글 첨부 이미지 엔티티.
 *
 * 게시글과 N:1 연관, 정렬 순서/캡션/이미지 URL을 보관합니다.
 */
@Entity
@Table(name = "post_images")
@Getter
@Setter
@NoArgsConstructor
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "caption")
    private String caption;
}
