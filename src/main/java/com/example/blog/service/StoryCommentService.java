package com.example.blog.service;

import com.example.blog.domain.StoryComment;
import com.example.blog.domain.Story;
import com.example.blog.repository.StoryCommentRepository;
import com.example.blog.repository.StoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 스토리 댓글 서비스
 */
@Service
public class StoryCommentService {
    private final StoryCommentRepository storyCommentRepository;
    private final StoryRepository storyRepository;

    public StoryCommentService(StoryCommentRepository storyCommentRepository, StoryRepository storyRepository) {
        this.storyCommentRepository = storyCommentRepository;
        this.storyRepository = storyRepository;
    }

    /**
     * 특정 스토리의 댓글 목록 조회
     */
    public List<StoryComment> getCommentsByStoryId(Long storyId) {
        return storyCommentRepository.findByStoryIdAndIsDeletedFalseOrderByCreatedAtDesc(storyId);
    }

    /**
     * 특정 스토리의 댓글 수
     */
    public long getCommentCount(Long storyId) {
        return storyCommentRepository.countByStoryIdAndIsDeletedFalse(storyId);
    }

    /**
     * 댓글 작성
     */
    @Transactional
    public StoryComment createComment(Long storyId, String content, String authorNickname, String authorUsername) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("스토리를 찾을 수 없습니다."));

        StoryComment comment = new StoryComment();
        comment.setStory(story);
        comment.setContent(content);
        comment.setAuthor(authorNickname);
        comment.setAuthorUsername(authorUsername);

        return storyCommentRepository.save(comment);
    }

    /**
     * 댓글 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteComment(Long commentId, String username) {
        StoryComment comment = storyCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 본인만 삭제 가능
        if (!comment.getAuthorUsername().equals(username)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        comment.setDeleted(true);
        storyCommentRepository.save(comment);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public void updateComment(Long commentId, String content, String username) {
        StoryComment comment = storyCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 본인만 수정 가능
        if (!comment.getAuthorUsername().equals(username)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        comment.setContent(content);
        storyCommentRepository.save(comment);
    }
}
