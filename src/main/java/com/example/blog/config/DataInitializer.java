package com.example.blog.config;

import com.example.blog.domain.Member;
import com.example.blog.domain.Role;
import com.example.blog.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

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
