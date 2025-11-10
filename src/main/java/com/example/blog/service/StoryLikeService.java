package com.example.blog.service;

import com.example.blog.domain.Story;
import com.example.blog.domain.StoryLike;
import com.example.blog.repository.StoryLikeRepository;
import com.example.blog.repository.StoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 스토리 좋아요 서비스.
 *
 * 좋아요 토글과 개수/상태 조회를 제공합니다.
 */
@Service
public class StoryLikeService {
    private final StoryLikeRepository storyLikeRepository;
    private final StoryRepository storyRepository;

    public StoryLikeService(StoryLikeRepository storyLikeRepository, StoryRepository storyRepository) {
        this.storyLikeRepository = storyLikeRepository;
        this.storyRepository = storyRepository;
    }

    /**
     * 스토리 좋아요를 토글합니다.
     *
     * @param storyId  스토리 식별자
     * @param username 사용자명
     * @return 토글 후 좋아요 상태(true=좋아요)
     */
    @Transactional
    public boolean toggleLike(Long storyId, String username) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("스토리를 찾을 수 없습니다."));

        var existing = storyLikeRepository.findByStoryIdAndUsername(storyId, username);
        if (existing.isPresent()) {
            storyLikeRepository.delete(existing.get());
            story.setLikeCount(Math.max(0L, (story.getLikeCount() == null ? 0L : story.getLikeCount()) - 1));
            storyRepository.save(story);
            return false;
        } else {
            StoryLike like = new StoryLike();
            like.setStory(story);
            like.setUsername(username);
            storyLikeRepository.save(like);
            story.setLikeCount((story.getLikeCount() == null ? 0L : story.getLikeCount()) + 1);
            storyRepository.save(story);
            return true;
        }
    }

    /**
     * 스토리 좋아요 수를 조회합니다.
     *
     * @param storyId 스토리 식별자
     * @return 좋아요 수
     */
    public long getLikeCount(Long storyId) {
        return storyLikeRepository.countByStoryId(storyId);
    }

    /**
     * 사용자가 해당 스토리에 좋아요를 눌렀는지 확인합니다.
     *
     * @param storyId  스토리 식별자
     * @param username 사용자명
     * @return 좋아요 여부
     */
    public boolean isLiked(Long storyId, String username) {
        return storyLikeRepository.existsByStoryIdAndUsername(storyId, username);
    }
}
