package com.example.blog.config;

import com.example.blog.domain.Member;
import com.example.blog.domain.Role;
import com.example.blog.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 개발/로컬 환경용 초기 데이터 설정.
 *
 * 애플리케이션 기동 시 관리자 계정을 생성합니다(존재하지 않을 경우).
 */
@Configuration
public class DataInitializer {

    /**
     * 관리자 계정을 초기화합니다.
     *
     * @param memberRepository 회원 리포지토리
     * @return 커맨드라인 러너
     */
    @Bean
    public CommandLineRunner initAdmin(MemberRepository memberRepository) {
        return args -> {
            if (memberRepository.findByUsername("admin") == null) {
                Member admin = new Member();
                admin.setUsername("admin");
                admin.setPassword("1111");
                admin.setNickname("관리자");
                admin.setEmail("admin@example.com");
                admin.setActive(true);
                admin.setRole(Role.ADMIN);
                memberRepository.save(admin);
                System.out.println("[INIT] Admin user created: admin / 1111");
            }
        };
    }
}
