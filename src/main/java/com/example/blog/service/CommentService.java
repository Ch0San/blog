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
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId);
    }

    // 댓글 작성
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
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    // 댓글 수정
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
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }
}
