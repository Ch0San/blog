package com.example.blog.controller;

import com.example.blog.domain.Notice;
import com.example.blog.service.NoticeService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notice")
@Validated
public class NoticeController {
    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    // 목록: 모두 열람 가능
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
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Notice n = noticeService.getNoticeAndIncrementViews(id);
        if (n == null)
            return "fragments/index_404"; // 간단 처리
        model.addAttribute("notice", n);
        return "notice/detail";
    }

    // 작성 폼: ADMIN만 접근 (Security에서 제한)
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("notice", new Notice());
        return "notice/write";
    }

    // 작성 처리: ADMIN만
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
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Notice n = noticeService.getNotice(id);
        if (n == null)
            return "fragments/index_404";
        model.addAttribute("notice", n);
        return "notice/edit";
    }

    // 수정 처리: ADMIN만
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
            @RequestParam @NotBlank String title,
            @RequestParam @NotBlank String author,
            @RequestParam @NotBlank String content) {
        noticeService.update(id, title, content, author);
        return "redirect:/notice/" + id;
    }

    // 삭제: ADMIN만
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        noticeService.delete(id);
        return "redirect:/notice";
    }
}
