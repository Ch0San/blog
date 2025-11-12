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
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

/**
 * Spring Security ?ㅼ젙 ?대옒??
 * ??蹂댁븞 諛??몄쬆/?멸? 洹쒖튃???뺤쓽?⑸땲??
 */
/**
 * Spring Security ?ㅼ젙 ?대옒??
 *
 * ?묎렐 ?쒖뼱 洹쒖튃, 濡쒓렇??濡쒓렇?꾩썐, remember-me ?ㅼ젙 ?깆쓣 援ъ꽦?⑸땲??
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                // ?됰Ц 鍮꾨?踰덊샇 ?ъ슜 (?뷀샇???놁쓬)
                // 二쇱쓽: ?ㅼ젣 ?댁쁺 ?섍꼍?먯꽌???덈? ?ъ슜?섎㈃ ???⑸땲??
                return NoOpPasswordEncoder.getInstance();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService)
                        throws Exception {
                http
                                .userDetailsService(userDetailsService)
                                .authorizeHttpRequests(auth -> auth
                                                // ?뺤쟻 由ъ냼?ㅼ뿉 ????묎렐 ?덉슜
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**",
                                                                "/uploads/**")
                                                .permitAll()
                                                // ?덊럹?댁?, ?뚯썝媛?? 濡쒓렇???섏씠吏??紐⑤몢 ?묎렐 媛??
                                                .requestMatchers("/", "/index.html", "/signup", "/login", "/error",
                                                                "/401")
                                                .permitAll()
                                                // 怨듭??ы빆 議고쉶??紐⑤몢 ?묎렐 媛??(紐⑸줉/?곸꽭)
                                                .requestMatchers(HttpMethod.GET,
                                                                "/notice",
                                                                "/notice/",
                                                                "/notice/*")
                                                .permitAll()
                                                // ?ㅽ넗由?議고쉶??紐⑤몢 ?묎렐 媛??(紐⑸줉/寃???곸꽭)
                                                .requestMatchers(HttpMethod.GET,
                                                                "/stories",
                                                                "/stories/",
                                                                "/stories/*",
                                                                "/stories/search/**")
                                                .permitAll()
                                                // 寃뚯떆湲 議고쉶??紐⑤몢 ?묎렐 媛??(GET留??덉슜)
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
                                                // ?ㅽ넗由??묒꽦, ?섏젙, ??젣??愿由ъ옄留?
                                                .requestMatchers(
                                                                "/stories/write",
                                                                "/stories/edit/**",
                                                                "/stories/delete/**")
                                                .hasRole("ADMIN")
                                                // ?뚯썝 愿??- ?뚯썝媛?낃낵 濡쒓렇?몃쭔 ?덉슜, ?뺣낫?섏젙/??젣???몄쬆 ?꾩슂
                                                .requestMatchers("/member/signup", "/member/signin").permitAll()
                                                // ?꾩씠??鍮꾨?踰덊샇 李얘린???몄쬆 ?놁씠 ?묎렐 媛??
                                                .requestMatchers("/member/find-id", "/member/find-password",
                                                                "/member/reset-password")
                                                .permitAll()
                                                .requestMatchers("/member/update", "/member/delete").authenticated()
                                                // ?뚯썝 紐⑸줉? 愿由ъ옄留?
                                                .requestMatchers("/member/list").hasRole("ADMIN")
                                                // ?쒓렇 愿由?諛??뚯썝 愿由?紐⑸줉/?섏젙/??젣)??愿由ъ옄留?
                                                .requestMatchers("/member/tag-update").hasRole("ADMIN")
                                                .requestMatchers("/member/admin/**").hasRole("ADMIN")
                                                // ?ъ씠???ㅼ젙(愿由ъ옄 ?꾩슜)
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                // 寃뚯떆湲 ?묒꽦, ?섏젙, ??젣??愿由ъ옄留?
                                                .requestMatchers(
                                                                "/posts/write",
                                                                "/posts/edit/**",
                                                                "/posts/delete/**")
                                                .hasRole("ADMIN")
                                                // 怨듭??ы빆 ?묒꽦, ?섏젙, ??젣??愿由ъ옄留?
                                                .requestMatchers(
                                                                "/notice/write",
                                                                "/notice/edit/**",
                                                                "/notice/delete/**")
                                                .hasRole("ADMIN")
                                                // ?먮뵒?????낅줈??API??愿由ъ옄留??덉슜
                                                .requestMatchers(HttpMethod.POST, "/api/uploads/**").hasRole("ADMIN")
                                                // ?볤? ?묒꽦/??젣???몄쬆???ъ슜?먮쭔
                                                .requestMatchers(HttpMethod.POST, "/posts/*/comments",
                                                                "/comments/*/delete")
                                                .authenticated()
                                                // 寃뚯떆湲 醫뗭븘?붾뒗 ?몄쬆???ъ슜?먮쭔
                                                .requestMatchers(HttpMethod.POST, "/posts/*/like").authenticated()
                                                // ?볤?/?듦? ?묒꽦? ?몄쬆 ?ъ슜???덉슜 (?붾뱶?ъ씤?멸? ?덉쓣 寃쎌슦)
                                                .requestMatchers(HttpMethod.POST, "/comments/**", "/replies/**")
                                                .authenticated()
                                                // ?섎㉧吏 ?붿껌? ?몄쬆 ?꾩슂
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .defaultAuthenticationEntryPointFor(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                                                new org.springframework.security.web.util.matcher.OrRequestMatcher(
                                                                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                                                                                                "/api/**"),
                                                                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                                                                                                "/posts/*/like"),
                                                                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                                                                                                "/comments/*/like"),
                                                                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                                                                                                "/stories/*/like"),
                                                                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                                                                                                "/stories/comments/*/like"),
                                                                                new org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher(
                                                                                                "X-Requested-With",
                                                                                                "XMLHttpRequest"))))
                                .formLogin(form -> form
                                                .loginPage("/member/signin") // 而ㅼ뒪? 濡쒓렇???섏씠吏
                                                .loginProcessingUrl("/member/signin") // 濡쒓렇??泥섎━ URL
                                                .defaultSuccessUrl("/", true) // 濡쒓렇???깃났 ???대룞???섏씠吏
                                                .failureUrl("/member/signin?error") // 濡쒓렇???ㅽ뙣 ???대룞???섏씠吏
                                                .permitAll())
                                .rememberMe(remember -> remember
                                                .key("blog-remember-me-key")
                                                .tokenValiditySeconds(60 * 60 * 24 * 14) // 14??
                                                .userDetailsService(userDetailsService))
                                .logout(logout -> logout
                                                .logoutUrl("/member/signout") // 濡쒓렇?꾩썐 URL
                                                .logoutSuccessUrl("/?logout") // 濡쒓렇?꾩썐 ?깃났 ???대룞???섏씠吏
                                                .permitAll());

                return http.build();
        }

        // UserDetailsService??CustomUserDetailsService(@Service) 鍮덉씠 ?먮룞 二쇱엯?⑸땲??
}
