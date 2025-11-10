package com.example.blog.service;

import com.example.blog.domain.Notice;
import com.example.blog.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공지사항(Notice) 비즈니스 로직 서비스.
 *
 * 목록/상세/조회수 증가/CRUD 및 최근 공지 조회를 제공합니다.
 */
@Service
public class NoticeService {
    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    /**
     * 공지사항 페이지 목록을 조회합니다.
     *
     * @param page 0부터 시작하는 페이지 번호
     * @param size 페이지 크기
     * @return 공지사항 페이지(최신 생성일 내림차순)
     */
    public Page<Notice> getNotices(int page, int size) {
        return noticeRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    /**
     * 공지사항을 ID로 조회합니다.
     *
     * @param id 공지사항 식별자
     * @return 공지사항 또는 null(없음)
     */
    public Notice getNotice(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }

    /**
     * 공지사항을 조회하고 조회수를 1 증가시킵니다.
     *
     * @param id 공지사항 식별자
     * @return 공지사항 또는 null(없음)
     */
    @Transactional
    public Notice getNoticeAndIncrementViews(Long id) {
        Notice n = noticeRepository.findById(id).orElse(null);
        if (n != null) {
            n.setViewCount(n.getViewCount() + 1);
            noticeRepository.save(n);
        }
        return n;
    }

    /**
     * 공지사항을 생성합니다.
     *
     * @param notice 공지사항 엔티티
     * @return 저장된 공지사항
     */
    @Transactional
    public Notice create(Notice notice) {
        return noticeRepository.save(notice);
    }

    /**
     * 공지사항을 수정합니다.
     *
     * @param id      공지사항 식별자
     * @param title   제목
     * @param content 본문
     * @param author  작성자 표시명
     * @return 수정된 공지사항 또는 null(없음)
     */
    @Transactional
    public Notice update(Long id, String title, String content, String author) {
        Notice n = noticeRepository.findById(id).orElse(null);
        if (n == null)
            return null;
        n.setTitle(title);
        n.setContent(content);
        n.setAuthor(author);
        return noticeRepository.save(n);
    }

    /**
     * 공지사항을 삭제합니다.
     *
     * @param id 공지사항 식별자
     */
    @Transactional
    public void delete(Long id) {
        noticeRepository.deleteById(id);
    }

    /**
     * 최근 공지사항을 조회합니다.
     *
     * @param limit 가져올 개수
     * @return 공지사항 목록
     */
    public List<Notice> getRecentNotices(int limit) {
        return noticeRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
    }
}
