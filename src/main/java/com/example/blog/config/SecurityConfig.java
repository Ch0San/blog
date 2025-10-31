package com.example.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정 클래스
 * 웹 보안 및 인증/인가 규칙을 정의합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                // 평문 비밀번호 사용 (암호화 없음)
                // 주의: 실제 운영 환경에서는 절대 사용하면 안 됩니다!
                return NoOpPasswordEncoder.getInstance();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService)
                        throws Exception {
                http
                                .userDetailsService(userDetailsService)
                                .authorizeHttpRequests(auth -> auth
                                                // 정적 리소스에 대한 접근 허용
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**",
                                                                "/uploads/**")
                                                .permitAll()
                                                // 홈페이지, 회원가입, 로그인 페이지는 모두 접근 가능
                                                .requestMatchers("/", "/index.html", "/signup", "/login").permitAll()
                                                // 공지사항 조회는 모두 접근 가능 (목록/상세)
                                                .requestMatchers(HttpMethod.GET,
                                                                "/notice",
                                                                "/notice/",
                                                                "/notice/*")
                                                .permitAll()
                                                // 스토리 조회는 모두 접근 가능 (목록/검색/상세)
                                                .requestMatchers(HttpMethod.GET,
                                                                "/stories",
                                                                "/stories/",
                                                                "/stories/*",
                                                                "/stories/search/**")
                                                .permitAll()
                                                // 게시글 조회는 모두 접근 가능 (GET만 허용)
                                                .requestMatchers(HttpMethod.GET,
                                                                "/posts",
                                                                "/posts/",
                                                                "/posts/*",
                                                                "/posts/view/**",
                                                                "/posts/list/**",
                                                                "/posts/search/**",
                                                                "/posts/download",
                                                                "/api/posts/**")
                                                .permitAll()
                                                // 스토리 작성, 수정, 삭제는 관리자만
                                                .requestMatchers(
                                                                "/stories/write",
                                                                "/stories/edit/**",
                                                                "/stories/delete/**")
                                                .hasRole("ADMIN")
                                                // 회원 관련 - 회원가입과 로그인만 허용, 정보수정/삭제는 인증 필요
                                                .requestMatchers("/member/signup", "/member/signin").permitAll()
                                                // 아이디/비밀번호 찾기는 인증 없이 접근 가능
                                                .requestMatchers("/member/find-id", "/member/find-password",
                                                                "/member/reset-password")
                                                .permitAll()
                                                .requestMatchers("/member/update", "/member/delete").authenticated()
                                                // 회원 목록은 관리자만
                                                .requestMatchers("/member/list").hasRole("ADMIN")
                                                // 태그 관리 및 회원 관리(목록/수정/삭제)는 관리자만
                                                .requestMatchers("/member/tag-update").hasRole("ADMIN")
                                                .requestMatchers("/member/admin/**").hasRole("ADMIN")
                                                // 사이트 설정(관리자 전용)
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                // 게시글 작성, 수정, 삭제는 관리자만
                                                .requestMatchers(
                                                                "/posts/write",
                                                                "/posts/edit/**",
                                                                "/posts/delete/**")
                                                .hasRole("ADMIN")
                                                // 공지사항 작성, 수정, 삭제는 관리자만
                                                .requestMatchers(
                                                                "/notice/write",
                                                                "/notice/edit/**",
                                                                "/notice/delete/**")
                                                .hasRole("ADMIN")
                                                // 에디터 내 업로드 API는 관리자만 허용
                                                .requestMatchers(HttpMethod.POST, "/api/uploads/**").hasRole("ADMIN")
                                                // 댓글 작성/삭제는 인증된 사용자만
                                                .requestMatchers(HttpMethod.POST, "/posts/*/comments",
                                                                "/comments/*/delete")
                                                .authenticated()
                                                // 게시글 좋아요는 인증된 사용자만
                                                .requestMatchers(HttpMethod.POST, "/posts/*/like").authenticated()
                                                // 댓글/답글 작성은 인증 사용자 허용 (엔드포인트가 있을 경우)
                                                .requestMatchers(HttpMethod.POST, "/comments/**", "/replies/**")
                                                .authenticated()
                                                // 나머지 요청은 인증 필요
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/member/signin") // 커스텀 로그인 페이지
                                                .loginProcessingUrl("/member/signin") // 로그인 처리 URL
                                                .defaultSuccessUrl("/", true) // 로그인 성공 시 이동할 페이지
                                                .failureUrl("/member/signin?error") // 로그인 실패 시 이동할 페이지
                                                .permitAll())
                                .rememberMe(remember -> remember
                                                .key("blog-remember-me-key")
                                                .tokenValiditySeconds(60 * 60 * 24 * 14) // 14일
                                                .userDetailsService(userDetailsService))
                                .logout(logout -> logout
                                                .logoutUrl("/member/signout") // 로그아웃 URL
                                                .logoutSuccessUrl("/?logout") // 로그아웃 성공 시 이동할 페이지
                                                .permitAll());

                return http.build();
        }

        // UserDetailsService는 CustomUserDetailsService(@Service) 빈이 자동 주입됩니다.
}
