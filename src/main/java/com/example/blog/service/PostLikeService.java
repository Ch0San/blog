package com.example.blog.service;

import com.example.blog.domain.Post;
import com.example.blog.domain.PostLike;
import com.example.blog.repository.PostLikeRepository;
import com.example.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 좋아요 서비스
 */
/**
 * 게시글 좋아요 서비스.
 *
 * 좋아요 토글, 상태 확인, 개수 조회를 제공합니다.
 */
@Service
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    public PostLikeService(PostLikeRepository postLikeRepository, PostRepository postRepository) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
    }

    // 좋아요 토글 (있으면 삭제, 없으면 추가)
    /**
     * 게시글 좋아요를 토글합니다.
     *
     * @param postId   게시글 식별자
     * @param username 사용자명
     * @return 토글 후 좋아요 상태(true=좋아요)
     */
    @Transactional
    public boolean toggleLike(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        var existingLike = postLikeRepository.findByPostIdAndUsername(postId, username);

        if (existingLike.isPresent()) {
            // 좋아요 취소
            postLikeRepository.delete(existingLike.get());
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
            return false;
        } else {
            // 좋아요 추가
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUsername(username);
            postLikeRepository.save(like);

            post.setLikeCount(post.getLikeCount() + 1);
            postRepository.save(post);
            return true;
        }
    }

    // 좋아요 여부 확인
    /**
     * 사용자가 해당 게시글에 좋아요를 눌렀는지 확인합니다.
     *
     * @param postId   게시글 식별자
     * @param username 사용자명
     * @return 좋아요 여부
     */
    public boolean isLiked(Long postId, String username) {
        return postLikeRepository.existsByPostIdAndUsername(postId, username);
    }

    // 좋아요 수 조회
    /**
     * 게시글 좋아요 수를 조회합니다.
     *
     * @param postId 게시글 식별자
     * @return 좋아요 수
     */
    public long getLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }
}
