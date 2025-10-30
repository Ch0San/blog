package com.example.blog.repository;

import com.example.blog.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 회원 레포지토리
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByUsername(String username);

    Member findByEmail(String email);

    Member findByEmailAndPhoneNumber(String email, String phoneNumber);

    Member findByUsernameAndEmailAndPhoneNumber(String username, String email, String phoneNumber);
}
