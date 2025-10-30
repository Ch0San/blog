package com.example.blog.config;

import com.example.blog.domain.Member;
import com.example.blog.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 뷰에서 현재 로그인한 회원 정보를 사용할 수 있도록 주입하는 어드바이스
 */
@ControllerAdvice
@Component
public class GlobalModelAttributes {

    private final MemberRepository memberRepository;
    private final String kakaoJsKey;

    public GlobalModelAttributes(
            MemberRepository memberRepository,
            @Value("${kakao.maps.javascript.key:}") String kakaoJsKey) {
        this.memberRepository = memberRepository;
        this.kakaoJsKey = kakaoJsKey;
    }

    @ModelAttribute("currentMember")
    public Member currentMember(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        return memberRepository.findByUsername(username);
    }

    /**
     * 카카오 지도 JavaScript 키를 전역 모델로 노출하여 템플릿에서 사용
     */
    @ModelAttribute("kakaoJsKey")
    public String kakaoJsKey() {
        return kakaoJsKey;
    }
}
