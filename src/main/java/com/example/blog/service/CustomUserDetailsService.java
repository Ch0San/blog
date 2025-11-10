package com.example.blog.service;

import com.example.blog.domain.Member;
import com.example.blog.repository.MemberRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 스프링 시큐리티 UserDetailsService 구현체.
 *
 * 회원 사용자명을 통해 사용자 정보를 로드하고, ROLE_접두사의 권한을 부여합니다.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 사용자명을 통해 사용자 정보를 로딩합니다.
     *
     * @param username 사용자명
     * @return 스프링 시큐리티 UserDetails
     * @throws UsernameNotFoundException 사용자를 찾지 못한 경우
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + member.getRole().name());
        return new User(
                member.getUsername(),
                member.getPassword(),
                member.isActive(),
                true,
                true,
                true,
                List.of(authority));
    }
}
