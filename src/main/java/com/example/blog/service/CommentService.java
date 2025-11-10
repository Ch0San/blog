package com.example.blog.service;

import com.example.blog.domain.Comment;
import com.example.blog.domain.Post;
import com.example.blog.domain.Member;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 댓글 서비스
 */
/**
 * 댓글(Comment) 비즈니스 로직 서비스.
 *
 * 생성/수정/삭제(소프트 삭제)와 조회 기능을 제공합니다.
 */
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository,
            MemberRepository memberRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
    }

    // 게시글의 댓글 목록 조회
    /**
     * 특정 게시글의 댓글 목록을 생성일 오름차순으로 조회합니다.
     * 삭제된 댓글은 제외합니다.
     *
     * @param postId 게시글 식별자
     * @return 댓글 목록
     */
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId);
    }

    // 댓글 작성
    /**
     * 댓글을 생성합니다.
     *
     * @param postId         게시글 식별자
     * @param content        댓글 내용
     * @param authorUsername 작성자 사용자명(인증 사용자)
     * @return 생성된 댓글
     * @throws IllegalArgumentException 게시글/회원 정보를 찾을 수 없는 경우
     */
    @Transactional
    public Comment createComment(Long postId, String content, String authorUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        Member member = memberRepository.findByUsername(authorUsername);
        if (member == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setContent(content);
        // 표시용 이름은 닉네임, 권한 확인용은 아이디 저장
        comment.setAuthor(member.getNickname());
        comment.setAuthorUsername(authorUsername);

        return commentRepository.save(comment);
    }

    // 댓글 삭제 (소프트 삭제)
    /**
     * 댓글을 소프트 삭제합니다.
     *
     * @param commentId 댓글 식별자
     * @throws IllegalArgumentException 댓글을 찾을 수 없는 경우
     */
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    // 댓글 수정
    /**
     * 댓글을 수정합니다. 작성자 본인만 수정할 수 있습니다.
     *
     * @param commentId 댓글 식별자
     * @param content   변경할 내용
     * @param username  요청자 사용자명(작성자 본인 확인용)
     * @return 수정된 댓글
     * @throws IllegalArgumentException 댓글이 없거나 작성자가 아닌 경우
     */
    @Transactional
    public Comment updateComment(Long commentId, String content, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 본인만 수정 가능
        if (comment.getAuthorUsername() == null || !comment.getAuthorUsername().equals(username)) {
            throw new IllegalArgumentException("댓글 작성자만 수정할 수 있습니다.");
        }

        comment.setContent(content);
        return commentRepository.save(comment);
    }

    // 댓글 조회 (단일)
    /**
     * 댓글 단건을 조회합니다.
     *
     * @param commentId 댓글 식별자
     * @return 댓글
     * @throws IllegalArgumentException 댓글을 찾을 수 없는 경우
     */
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }
}
