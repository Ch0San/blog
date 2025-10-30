package com.example.blog.service;

import com.example.blog.domain.Notice;
import com.example.blog.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoticeService {
    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public Page<Notice> getNotices(int page, int size) {
        return noticeRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public Notice getNotice(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }

    @Transactional
    public Notice getNoticeAndIncrementViews(Long id) {
        Notice n = noticeRepository.findById(id).orElse(null);
        if (n != null) {
            n.setViewCount(n.getViewCount() + 1);
            noticeRepository.save(n);
        }
        return n;
    }

    @Transactional
    public Notice create(Notice notice) {
        return noticeRepository.save(notice);
    }

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

    @Transactional
    public void delete(Long id) {
        noticeRepository.deleteById(id);
    }

    public List<Notice> getRecentNotices(int limit) {
        return noticeRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
    }
}
