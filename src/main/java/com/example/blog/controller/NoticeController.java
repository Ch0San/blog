package com.example.blog.controller;

import com.example.blog.domain.Notice;
import com.example.blog.service.NoticeService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 공지사항(Notice) 컨트롤러.
 *
 * 목록/상세는 공개, 작성/수정/삭제는 관리자 권한으로 제한됩니다.
 */
@Controller
@RequestMapping("/notice")
@Validated
public class NoticeController {
    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    // 목록: 모두 열람 가능
    /**
     * 공지사항 목록을 페이지로 조회합니다.
     *
     * @param page 0부터 시작하는 페이지 번호
     * @param size 페이지 크기
     * @param model 뷰 렌더링용 모델
     * @return 목록 뷰 이름(`notice/list`)
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<Notice> p = noticeService.getNotices(page, size);
        model.addAttribute("page", p);
        model.addAttribute("notices", p.getContent());
        return "notice/list";
    }

    // 상세: 모두 열람 가능 (조회수 증가)
    /**
     * 공지사항 상세를 조회합니다. 조회수는 1 증가합니다.
     *
     * @param id 공지사항 식별자
     * @param model 뷰 렌더링용 모델
     * @return 상세 뷰 이름(`notice/detail`), 없으면 404 조각 뷰
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Notice n = noticeService.getNoticeAndIncrementViews(id);
        if (n == null)
            return "fragments/index_404"; // 간단 처리
        model.addAttribute("notice", n);
        return "notice/detail";
    }

    // 작성 폼: ADMIN만 접근 (Security에서 제한)
    /**
     * 공지사항 작성 폼(관리자).
     *
     * @param model 뷰 렌더링용 모델
     * @return 작성 폼 뷰 이름(`notice/write`)
     */
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("notice", new Notice());
        return "notice/write";
    }

    // 작성 처리: ADMIN만
    /**
     * 공지사항 생성(관리자).
     *
     * @param title   제목(필수)
     * @param author  작성자 표시명(필수)
     * @param content 본문(필수)
     * @return 목록으로 리다이렉트
     */
    @PostMapping("/write")
    public String write(@RequestParam @NotBlank String title,
            @RequestParam @NotBlank String author,
            @RequestParam @NotBlank String content) {
        Notice n = new Notice();
        n.setTitle(title);
        n.setAuthor(author);
        n.setContent(content);
        noticeService.create(n);
        return "redirect:/notice";
    }

    // 수정 폼: ADMIN만
    /**
     * 공지사항 수정 폼(관리자).
     *
     * @param id 공지사항 식별자
     * @param model 뷰 렌더링용 모델
     * @return 수정 폼 뷰 이름(`notice/edit`), 없으면 404 조각 뷰
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Notice n = noticeService.getNotice(id);
        if (n == null)
            return "fragments/index_404";
        model.addAttribute("notice", n);
        return "notice/edit";
    }

    // 수정 처리: ADMIN만
    /**
     * 공지사항 수정(관리자).
     *
     * @param id 공지사항 식별자
     * @param title 제목(필수)
     * @param author 작성자 표시명(필수)
     * @param content 본문(필수)
     * @return 상세로 리다이렉트
     */
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
            @RequestParam @NotBlank String title,
            @RequestParam @NotBlank String author,
            @RequestParam @NotBlank String content) {
        noticeService.update(id, title, content, author);
        return "redirect:/notice/" + id;
    }

    // 삭제: ADMIN만
    /**
     * 공지사항 삭제(관리자).
     *
     * @param id 공지사항 식별자
     * @return 목록으로 리다이렉트
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        noticeService.delete(id);
        return "redirect:/notice";
    }
}
