package com.example.blog.repository;

import com.example.blog.domain.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {
    Optional<StoryLike> findByStoryIdAndUsername(Long storyId, String username);

    long countByStoryId(Long storyId);

    boolean existsByStoryIdAndUsername(Long storyId, String username);
}
