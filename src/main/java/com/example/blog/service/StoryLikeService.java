package com.example.blog.service;

import com.example.blog.domain.Story;
import com.example.blog.domain.StoryLike;
import com.example.blog.repository.StoryLikeRepository;
import com.example.blog.repository.StoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoryLikeService {
    private final StoryLikeRepository storyLikeRepository;
    private final StoryRepository storyRepository;

    public StoryLikeService(StoryLikeRepository storyLikeRepository, StoryRepository storyRepository) {
        this.storyLikeRepository = storyLikeRepository;
        this.storyRepository = storyRepository;
    }

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

    public long getLikeCount(Long storyId) {
        return storyLikeRepository.countByStoryId(storyId);
    }

    public boolean isLiked(Long storyId, String username) {
        return storyLikeRepository.existsByStoryIdAndUsername(storyId, username);
    }
}
