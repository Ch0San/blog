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
/**
 * 스토리 댓글 서비스.
 *
 * 스토리 댓글 조회/개수/생성/수정/삭제(소프트 삭제)를 제공합니다.
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
    /**
     * 특정 스토리의 댓글 목록을 최신 생성일 순서로 조회합니다.
     * 삭제된 댓글은 제외합니다.
     *
     * @param storyId 스토리 식별자
     * @return 댓글 목록
     */
    public List<StoryComment> getCommentsByStoryId(Long storyId) {
        return storyCommentRepository.findByStoryIdAndIsDeletedFalseOrderByCreatedAtDesc(storyId);
    }

    /**
     * 특정 스토리의 댓글 수
     */
    /**
     * 특정 스토리의 댓글 수를 조회합니다(삭제 제외).
     *
     * @param storyId 스토리 식별자
     * @return 댓글 수
     */
    public long getCommentCount(Long storyId) {
        return storyCommentRepository.countByStoryIdAndIsDeletedFalse(storyId);
    }

    /**
     * 댓글 작성
     */
    /**
     * 댓글을 생성합니다.
     *
     * @param storyId        스토리 식별자
     * @param content        내용
     * @param authorNickname 작성자 표시명
     * @param authorUsername 작성자 사용자명
     * @return 생성된 댓글
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
    /**
     * 댓글을 소프트 삭제합니다. 작성자 본인만 삭제할 수 있습니다.
     *
     * @param commentId 댓글 식별자
     * @param username  요청자 사용자명
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
    /**
     * 댓글을 수정합니다. 작성자 본인만 수정할 수 있습니다.
     *
     * @param commentId 댓글 식별자
     * @param content   내용
     * @param username  요청자 사용자명
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
