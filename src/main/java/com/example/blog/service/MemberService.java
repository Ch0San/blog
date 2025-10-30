package com.example.blog.service;

import com.example.blog.domain.Member;
import com.example.blog.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회원 서비스
 */
@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member saveMember(Member member) {
        // 비밀번호를 평문으로 그대로 저장
        return memberRepository.save(member);
    }

    @Transactional
    public Member updateMember(Member member) {
        // 비밀번호를 평문으로 그대로 저장
        return memberRepository.save(member);
    }

    @Transactional
    public Member updateMemberWithoutPasswordEncoding(Member member) {
        // 비밀번호는 이미 평문 상태로 전달됨
        return memberRepository.save(member);
    }

    public String encodePassword(String rawPassword) {
        // 평문 그대로 반환
        return rawPassword;
    }

    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findById(Long id) {
        return memberRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteById(Long id) {
        memberRepository.deleteById(id);
    }

    public boolean checkPassword(String rawPassword, String storedPassword) {
        // 평문 비교
        return rawPassword.equals(storedPassword);
    }

    @Transactional
    public void deleteByUsername(String username) {
        Member member = memberRepository.findByUsername(username);
        if (member != null) {
            memberRepository.delete(member);
        }
    }

    /**
     * 아이디 찾기: 이메일과 연락처로 회원 조회
     */
    public Member findByEmailAndPhoneNumber(String email, String phoneNumber) {
        return memberRepository.findByEmailAndPhoneNumber(email, phoneNumber);
    }

    /**
     * 비밀번호 찾기: 아이디, 이메일, 연락처로 회원 조회
     */
    public Member findByUsernameAndEmailAndPhoneNumber(String username, String email, String phoneNumber) {
        return memberRepository.findByUsernameAndEmailAndPhoneNumber(username, email, phoneNumber);
    }

    /**
     * 비밀번호 재설정
     */
    @Transactional
    public void resetPassword(String username, String newPassword) {
        Member member = memberRepository.findByUsername(username);
        if (member != null) {
            member.setPassword(newPassword);
            memberRepository.save(member);
        }
    }
}
