package com.example.blog.controller;

import com.example.blog.domain.Member;
import com.example.blog.service.MemberService;
import com.example.blog.service.SiteSettingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 회원 관련 컨트롤러
 */
/**
 * 회원 관리 컨트롤러.
 *
 * 회원 가입/로그인 페이지 노출, 정보 수정·삭제, 사이트 설정(소개/태그) 관리 및
 * 관리자에 의한 회원 목록/권한 관리 등을 담당합니다.
 */
@Controller
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final SiteSettingService siteSettingService;

    public MemberController(MemberService memberService, SiteSettingService siteSettingService) {
        this.memberService = memberService;
        this.siteSettingService = siteSettingService;
    }

    // 회원가입 페이지
    /**
     * 회원 가입 폼을 표시합니다.
     *
     * @return 뷰 이름(`member/signUp`)
     */
    @GetMapping("/signup")
    public String signupForm() {
        return "member/signUp";
    }

    // 회원가입 처리
    /**
     * 회원 가입을 처리합니다.
     *
     * @param username    아이디
     * @param password    비밀번호(평문 전달, 서비스에서 인코딩 권장)
     * @param nickname    닉네임
     * @param email       이메일
     * @param phoneNumber 전화번호(선택)
     * @param address     주소(선택)
     * @return 홈으로 리다이렉트
     */
    @PostMapping("/signup")
    public String signup(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String nickname,
            @RequestParam String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address) {
        Member member = new Member();
        member.setUsername(username);
        member.setPassword(password); // 평문 그대로 저장
        member.setNickname(nickname);
        member.setEmail(email);
        member.setPhoneNumber(phoneNumber);
        member.setAddress(address);

        memberService.saveMember(member);

        return "redirect:/";
    }

    // 로그인 페이지
    /**
     * 로그인 폼을 표시합니다.
     *
     * @return 뷰 이름(`member/signIn`)
     */
    @GetMapping("/signin")
    public String signinForm() {
        return "member/signIn";
    }

    // 정보수정 페이지
    /**
     * 내 정보 수정 폼을 표시합니다.
     *
     * @param userDetails 인증 사용자
     * @param model       뷰 렌더링용 모델
     * @return 뷰 이름(`member/memberUpdate`)
     */
    @GetMapping("/update")
    public String updateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        Member member = memberService.findByUsername(username);
        model.addAttribute("member", member);
        return "member/memberUpdate";
    }

    // 정보수정 처리
    /**
     * 내 정보를 수정합니다.
     *
     * @param userDetails     인증 사용자
     * @param currentPassword 현재 비밀번호(검증용)
     * @param password        변경할 비밀번호(선택)
     * @param email           이메일
     * @param phoneNumber     전화번호(선택)
     * @param address         주소(선택)
     * @return 성공/실패 쿼리 파라미터와 함께 폼으로 리다이렉트
     */
    @PostMapping("/update")
    public String update(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String currentPassword,
            @RequestParam(required = false) String password,
            @RequestParam String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address) {
        String username = userDetails.getUsername();
        Member member = memberService.findByUsername(username);

        // 현재 비밀번호 확인 (평문 비교)
        if (!memberService.checkPassword(currentPassword, member.getPassword())) {
            return "redirect:/member/update?error";
        }

        // 정보 업데이트
        member.setEmail(email);
        member.setPhoneNumber(phoneNumber);
        member.setAddress(address);

        // 새 비밀번호가 입력된 경우에만 변경 (평문 저장)
        if (password != null && !password.isEmpty()) {
            member.setPassword(password);
        }

        memberService.updateMemberWithoutPasswordEncoding(member);

        return "redirect:/member/update?success";
    }

    // 회원 탈퇴 처리
    /**
     * 회원 탈퇴를 처리합니다. 세션 종료 및 remember-me 쿠키를 제거합니다.
     *
     * @param userDetails 인증 사용자(미인증이면 로그인으로 이동)
     * @param request     HTTP 요청(로그아웃 처리)
     * @param response    HTTP 응답(쿠키 제거)
     * @return 홈으로 리다이렉트
     */
    @PostMapping("/delete")
    public String deleteAccount(@AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (userDetails == null) {
            return "redirect:/member/signin";
        }

        String username = userDetails.getUsername();
        memberService.deleteByUsername(username);

        // 로그아웃 및 세션 무효화
        try {
            request.logout();
        } catch (ServletException ignored) {
        }
        SecurityContextHolder.clearContext();

        // remember-me 쿠키 제거
        Cookie rm = new Cookie("remember-me", "");
        rm.setPath("/");
        rm.setMaxAge(0);
        response.addCookie(rm);

        return "redirect:/?accountDeleted";
    }

    // 태그 관리 페이지 (ADMIN 전용)
    /**
     * 사이트 태그 설정 폼(관리자).
     *
     * @param model 뷰 렌더링용 모델
     * @return 뷰 이름(`member/tagUpdate`)
     */
    @GetMapping("/tag-update")
    public String tagUpdateForm(Model model) {
        String tags = siteSettingService.getSetting("site_tags", "Java,Spring,MyBatis");
        model.addAttribute("tags", tags);
        return "member/tagUpdate";
    }

    // 태그 저장 처리 (ADMIN 전용)
    /**
     * 사이트 태그 설정 저장(관리자).
     *
     * @param tags 콤마로 구분된 태그 목록
     * @return 홈으로 리다이렉트(+표시용 쿼리)
     */
    @PostMapping("/tag-update")
    public String tagUpdate(@RequestParam String tags) {
        siteSettingService.saveSetting("site_tags", tags, "사이트 태그 목록");
        return "redirect:/?tagUpdated";
    }

    // 소개 관리 페이지 (ADMIN 전용)
    /**
     * 사이트 소개 설정 폼(관리자).
     *
     * @param model 뷰 렌더링용 모델
     * @return 뷰 이름(`member/introductionUpdate`)
     */
    @GetMapping("/introduction-update")
    public String introductionUpdateForm(Model model) {
        String introduction = siteSettingService.getSetting("site_introduction",
                "안녕하세요! 👋\n초보 개발자입니다.\n개발 공부하면서 배운 내용과\n일상, 여행 이야기를 기록합니다.");
        model.addAttribute("introduction", introduction);
        return "member/introductionUpdate";
    }

    // 소개 저장 처리 (ADMIN 전용)
    /**
     * 사이트 소개 설정 저장(관리자).
     *
     * @param introduction 소개 문구(멀티라인 허용)
     * @return 홈으로 리다이렉트(+표시용 쿼리)
     */
    @PostMapping("/introduction-update")
    public String introductionUpdate(@RequestParam String introduction) {
        siteSettingService.saveSetting("site_introduction", introduction, "사이트 소개");
        return "redirect:/?introductionUpdated";
    }

    // 회원 목록 보기 (ADMIN 전용)
    /**
     * 회원 목록을 조회합니다(관리자).
     *
     * @param model 뷰 렌더링용 모델
     * @return 뷰 이름(`member/list`)
     */
    @GetMapping("/list")
    public String list(Model model) {
        java.util.List<Member> members = memberService.findAll();
        model.addAttribute("members", members);
        return "member/list";
    }

    // 회원 수정 폼 (ADMIN 전용)
    /**
     * 회원 관리 수정 폼(관리자).
     *
     * @param id    회원 식별자
     * @param model 뷰 렌더링용 모델(역할 목록 포함)
     * @return 뷰 이름(`member/adminEdit`), 없으면 목록으로 리다이렉트
     */
    @GetMapping("/admin/edit/{id}")
    public String adminEditForm(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Member member = memberService.findById(id);
        if (member == null) {
            return "redirect:/member/list";
        }
        model.addAttribute("member", member);
        model.addAttribute("roles", com.example.blog.domain.Role.values());
        return "member/adminEdit";
    }

    // 회원 수정 처리 (ADMIN 전용)
    /**
     * 회원 관리 수정 처리(관리자).
     *
     * @param id          회원 식별자
     * @param nickname    닉네임
     * @param email       이메일
     * @param phoneNumber 전화번호(선택)
     * @param address     주소(선택)
     * @param role        역할
     * @param active      활성 여부
     * @param password    새 비밀번호(선택; 제공 시 평문 -> 인코딩 필요)
     * @return 목록으로 리다이렉트(+표시용 쿼리)
     */
    @PostMapping("/admin/edit/{id}")
    public String adminEdit(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @RequestParam String nickname,
            @RequestParam String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam com.example.blog.domain.Role role,
            @RequestParam(name = "active", defaultValue = "true") boolean active,
            @RequestParam(required = false) String password) {
        Member member = memberService.findById(id);
        if (member == null) {
            return "redirect:/member/list";
        }
        member.setNickname(nickname);
        member.setEmail(email);
        member.setPhoneNumber(phoneNumber);
        member.setAddress(address);
        member.setRole(role);
        member.setActive(active);
        if (password != null && !password.isEmpty()) {
            member.setPassword(password); // 평문 저장 정책
        }
        memberService.updateMemberWithoutPasswordEncoding(member);
        return "redirect:/member/list?updated";
    }

    // 회원 삭제 (ADMIN 전용)
    /**
     * 회원 삭제(관리자).
     *
     * @param id 회원 식별자
     * @return 목록으로 리다이렉트(+표시용 쿼리)
     */
    @PostMapping("/admin/delete/{id}")
    public String adminDelete(@org.springframework.web.bind.annotation.PathVariable Long id) {
        memberService.deleteById(id);
        return "redirect:/member/list?deleted";
    }

    // 아이디 찾기 페이지
    @GetMapping("/find-id")
    public String findIdForm() {
        return "member/findId";
    }

    // 아이디 찾기 처리
    @PostMapping("/find-id")
    public String findId(
            @RequestParam String email,
            @RequestParam String phoneNumber,
            Model model) {
        Member member = memberService.findByEmailAndPhoneNumber(email, phoneNumber);

        if (member != null) {
            model.addAttribute("foundUsername", member.getUsername());
            return "member/findId";
        } else {
            model.addAttribute("error", "일치하는 회원 정보를 찾을 수 없습니다.");
            return "member/findId";
        }
    }

    // 비밀번호 찾기 페이지
    @GetMapping("/find-password")
    public String findPasswordForm() {
        return "member/findPassword";
    }

    // 비밀번호 찾기 처리
    @PostMapping("/find-password")
    public String findPassword(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            Model model) {
        Member member = memberService.findByUsernameAndEmailAndPhoneNumber(username, email, phoneNumber);

        if (member != null) {
            model.addAttribute("verified", true);
            model.addAttribute("username", username);
            return "member/findPassword";
        } else {
            model.addAttribute("error", "일치하는 회원 정보를 찾을 수 없습니다.");
            return "member/findPassword";
        }
    }

    // 비밀번호 재설정 처리
    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String username,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("verified", true);
            model.addAttribute("username", username);
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "member/findPassword";
        }

        memberService.resetPassword(username, newPassword);
        return "redirect:/member/signin?passwordReset";
    }

}
