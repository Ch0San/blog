package com.example.blog.repository;

import com.example.blog.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 회원 레포지토리
 */
/**
 * 회원 리포지토리.
 *
 * 사용자명/이메일/연락처 기반 조회 메서드를 제공합니다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByUsername(String username);

    Member findByEmail(String email);

    Member findByEmailAndPhoneNumber(String email, String phoneNumber);

    Member findByUsernameAndEmailAndPhoneNumber(String username, String email, String phoneNumber);
}
