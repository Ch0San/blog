package com.example.blog.repository;

import com.example.blog.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * PostLike 리포지토리.
 */
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    // 특정 게시글에 대한 사용자의 좋아요 조회
    Optional<PostLike> findByPostIdAndUsername(Long postId, String username);

    // 특정 게시글의 좋아요 수 조회
    long countByPostId(Long postId);

    // 좋아요 존재 여부 확인
    boolean existsByPostIdAndUsername(Long postId, String username);
}
