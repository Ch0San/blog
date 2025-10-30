package com.example.blog.controller;

import com.example.blog.domain.Member;
import com.example.blog.service.PostService;
import com.example.blog.service.NoticeService;
import com.example.blog.service.SiteSettingService;
import com.example.blog.service.VisitorCountService;
import com.example.blog.service.MemberService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;

@Controller
public class HomeController {

    private final PostService postService;
    private final NoticeService noticeService;
    private final VisitorCountService visitorCountService;
    private final SiteSettingService siteSettingService;
    private final MemberService memberService;

    public HomeController(PostService postService, VisitorCountService visitorCountService,
            SiteSettingService siteSettingService, MemberService memberService, NoticeService noticeService) {
        this.postService = postService;
        this.visitorCountService = visitorCountService;
        this.siteSettingService = siteSettingService;
        this.memberService = memberService;
        this.noticeService = noticeService;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model,
            HttpServletRequest request, HttpServletResponse response) {
        // 새로고침이 아닌 '브라우저 방문 기준'으로 today/total 증가: 하루에 한 번만 카운트
        String cookieName = "visited_today";
        String todayStr = LocalDate.now().toString();
        boolean alreadyCountedToday = false;

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (cookieName.equals(c.getName()) && todayStr.equals(c.getValue())) {
                    alreadyCountedToday = true;
                    break;
                }
            }
        }

        if (!alreadyCountedToday) {
            visitorCountService.incrementTodayVisitor();

            // 오늘 날짜로 쿠키 설정 (유효기간: 오늘 자정까지)
            Cookie visited = new Cookie(cookieName, todayStr);
            visited.setPath("/");
            // 자정까지 남은 초 계산
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime midnight = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            long secondsToMidnight = Math.max(1, Duration.between(now, midnight).getSeconds());
            visited.setMaxAge((int) secondsToMidnight);
            visited.setHttpOnly(false); // 클라이언트에서 굳이 접근할 필요 없지만, 통계 쿠키이므로 false 유지
            response.addCookie(visited);
        }

        // 로그인한 사용자의 닉네임 가져오기
        if (userDetails != null) {
            Member member = memberService.findByUsername(userDetails.getUsername());
            model.addAttribute("currentMember", member);
        }

        // 인기글 4개 (조회수 순)
        var popularPosts = postService.getPopularPosts(4);
        popularPosts.forEach(post -> {
            if (post.getContent() != null) {
                String plainText = stripHtmlTags(post.getContent());
                post.setContent(plainText);
            }
        });
        model.addAttribute("popularPosts", popularPosts);

        // 최신글 4개 (최근 작성순)
        var recentPosts = postService.getRecentPosts(4);
        recentPosts.forEach(post -> {
            if (post.getContent() != null) {
                String plainText = stripHtmlTags(post.getContent());
                post.setContent(plainText);
            }
        });
        model.addAttribute("recentPosts", recentPosts);
        // 전체 게시글 수
        model.addAttribute("totalPosts", postService.getTotalPostCount());
        // 최근 공지 5개
        model.addAttribute("recentNotices", noticeService.getRecentNotices(5));
        // 방문자 수 (일별 고유 방문자 기준)
        model.addAttribute("todayVisitors", visitorCountService.getTodayVisitorCount());
        model.addAttribute("totalVisitors", visitorCountService.getTotalVisitorCount());
        // 태그 목록
        model.addAttribute("tags", siteSettingService.getSetting("site_tags", "Java,Spring,MyBatis"));
        // 소개 내용
        model.addAttribute("introduction", siteSettingService.getSetting("site_introduction",
                "안녕하세요! 👋\n초보 개발자입니다.\n개발 공부하면서 배운 내용과\n일상, 여행 이야기를 기록합니다."));
        // 히어로 이미지 URL (관리자가 업로드로 변경 가능, 기본값은 정적 이미지)
        model.addAttribute("heroImageUrl",
                siteSettingService.getSetting("site_hero_image_url", "/images/index_image.jpg"));

        return "index";
    }

    // HTML 태그 제거 유틸리티 메서드
    private String stripHtmlTags(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "");
    }
}
