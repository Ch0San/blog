package com.example.blog.service;

import com.example.blog.domain.StoryComment;
import com.example.blog.domain.StoryCommentLike;
import com.example.blog.repository.StoryCommentLikeRepository;
import com.example.blog.repository.StoryCommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 스토리 댓글 좋아요 서비스
 */
/**
 * 스토리 댓글 좋아요 서비스.
 *
 * 좋아요 토글, 상태 확인, 개수 조회, 사용자가 좋아요한 스토리 댓글 ID 집합 조회를 제공합니다.
 */
@Service
public class StoryCommentLikeService {
    private final StoryCommentLikeRepository storyCommentLikeRepository;
    private final StoryCommentRepository storyCommentRepository;

    public StoryCommentLikeService(StoryCommentLikeRepository storyCommentLikeRepository,
            StoryCommentRepository storyCommentRepository) {
        this.storyCommentLikeRepository = storyCommentLikeRepository;
        this.storyCommentRepository = storyCommentRepository;
    }

    // 좋아요 토글 (있으면 취소, 없으면 추가)
    /**
     * 스토리 댓글 좋아요를 토글합니다.
     *
     * @param commentId 댓글 식별자
     * @param username  사용자명
     * @return 토글 후 좋아요 상태(true=좋아요)
     */
    @Transactional
    public boolean toggleLike(Long commentId, String username) {
        StoryComment comment = storyCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        var existing = storyCommentLikeRepository.findByCommentIdAndUsername(commentId, username);
        if (existing.isPresent()) {
            storyCommentLikeRepository.delete(existing.get());
            long current = comment.getLikeCount() == null ? 0L : comment.getLikeCount();
            long newCount = Math.max(0L, current - 1L);
            comment.setLikeCount(newCount);
            storyCommentRepository.save(comment);
            return false;
        } else {
            StoryCommentLike like = new StoryCommentLike();
            like.setComment(comment);
            like.setUsername(username);
            storyCommentLikeRepository.save(like);

            long current = comment.getLikeCount() == null ? 0L : comment.getLikeCount();
            comment.setLikeCount(current + 1L);
            storyCommentRepository.save(comment);
            return true;
        }
    }

    /**
     * 사용자가 해당 스토리 댓글에 좋아요를 눌렀는지 확인합니다.
     */
    public boolean isLiked(Long commentId, String username) {
        return storyCommentLikeRepository.existsByCommentIdAndUsername(commentId, username);
    }

    /**
     * 스토리 댓글 좋아요 수를 조회합니다.
     */
    public long getLikeCount(Long commentId) {
        return storyCommentLikeRepository.countByCommentId(commentId);
    }

    // 스토리 내에서 사용자가 좋아요한 댓글 ID 집합
    /**
     * 특정 스토리에서 사용자가 좋아요한 댓글의 ID 집합을 조회합니다.
     *
     * @param storyId  스토리 식별자
     * @param username 사용자명
     * @return 댓글 ID 집합
     */
    public Set<Long> getLikedCommentIdsForStory(Long storyId, String username) {
        return storyCommentLikeRepository.findAllByComment_Story_IdAndUsername(storyId, username)
                .stream()
                .map(l -> l.getComment().getId())
                .collect(Collectors.toSet());
    }
}
