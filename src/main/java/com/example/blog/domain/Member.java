package com.example.blog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 회원 정보 엔티티
 * 블로그 사용자의 기본 정보를 저장하는 테이블
 */
/**
 * 회원 엔티티.
 *
 * 블로그 사용자의 기본 정보, 상태, 권한을 보관합니다.
 */
@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
public class Member {
    /** 회원 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 로그인 아이디 */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** 비밀번호 (평문 저장) */
    @Column(nullable = false, length = 255)
    private String password;

    /** 닉네임 */
    @Column(nullable = false, length = 50)
    private String nickname;

    /** 이메일 주소 */
    @Column(nullable = false, unique = true)
    private String email;

    /** 전화번호 */
    @Column(name = "phone_number")
    private String phoneNumber;

    /** 주소 */
    private String address;

    /** 계정 활성화 여부 */
    @Column(name = "is_active", columnDefinition = "boolean default true")
    private boolean isActive = true;

    /** 마지막 로그인 시간 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /** 가입일시 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 정보 수정일시 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 권한(역할) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;
}
