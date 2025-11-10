package com.example.blog.service;

import com.example.blog.domain.Member;
import com.example.blog.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회원 서비스
 */
/**
 * 회원(Member) 비즈니스 로직 서비스.
 *
 * 회원 저장/수정/삭제, 조회, 비밀번호 검증/리셋 등을 제공합니다.
 */
@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    /**
     * 회원을 저장합니다.
     *
     * @param member 회원 엔티티(비밀번호 인코딩은 별도 처리 가정)
     * @return 저장된 회원
     */
    public Member saveMember(Member member) {
        // 비밀번호를 평문으로 그대로 저장
        return memberRepository.save(member);
    }

    @Transactional
    /**
     * 회원 정보를 수정합니다.
     *
     * @param member 회원 엔티티(비밀번호 인코딩은 별도 처리 가정)
     * @return 수정된 회원
     */
    public Member updateMember(Member member) {
        // 비밀번호를 평문으로 그대로 저장
        return memberRepository.save(member);
    }

    @Transactional
    /**
     * 비밀번호 인코딩 없이 회원 정보를 저장/수정합니다.
     *
     * @param member 회원 엔티티(비밀번호 평문 유지)
     * @return 저장된 회원
     */
    public Member updateMemberWithoutPasswordEncoding(Member member) {
        // 비밀번호는 이미 평문 상태로 전달됨
        return memberRepository.save(member);
    }

    /**
     * 비밀번호 인코딩(현재는 패스스루; 보안 강화 필요).
     *
     * @param rawPassword 평문 비밀번호
     * @return 인코딩 결과(현재는 평문 그대로)
     */
    public String encodePassword(String rawPassword) {
        // 평문 그대로 반환
        return rawPassword;
    }

    /**
     * 사용자명을 통해 회원을 조회합니다.
     *
     * @param username 사용자명
     * @return 회원 또는 null
     */
    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    /**
     * 전체 회원 목록을 조회합니다.
     *
     * @return 회원 목록
     */
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    /**
     * 회원을 ID로 조회합니다.
     *
     * @param id 회원 식별자
     * @return 회원 또는 null
     */
    public Member findById(Long id) {
        return memberRepository.findById(id).orElse(null);
    }

    @Transactional
    /**
     * 회원을 ID로 삭제합니다.
     *
     * @param id 회원 식별자
     */
    public void deleteById(Long id) {
        memberRepository.deleteById(id);
    }

    /**
     * 비밀번호 검증(현재는 평문 비교; 보안 강화 필요).
     *
     * @param rawPassword    입력 평문
     * @param storedPassword 저장된 값
     * @return 일치 여부
     */
    public boolean checkPassword(String rawPassword, String storedPassword) {
        // 평문 비교
        return rawPassword.equals(storedPassword);
    }

    @Transactional
    /**
     * 사용자명으로 회원을 삭제합니다.
     *
     * @param username 사용자명
     */
    public void deleteByUsername(String username) {
        Member member = memberRepository.findByUsername(username);
        if (member != null) {
            memberRepository.delete(member);
        }
    }

    /**
     * 아이디 찾기: 이메일과 연락처로 회원 조회
     */
    /**
     * 아이디 찾기: 이메일과 연락처로 회원을 조회합니다.
     *
     * @param email       이메일
     * @param phoneNumber 연락처
     * @return 회원 또는 null
     */
    public Member findByEmailAndPhoneNumber(String email, String phoneNumber) {
        return memberRepository.findByEmailAndPhoneNumber(email, phoneNumber);
    }

    /**
     * 비밀번호 찾기: 아이디, 이메일, 연락처로 회원 조회
     */
    /**
     * 비밀번호 찾기: 아이디, 이메일, 연락처로 회원을 조회합니다.
     *
     * @param username 사용자명
     * @param email    이메일
     * @param phoneNumber 연락처
     * @return 회원 또는 null
     */
    public Member findByUsernameAndEmailAndPhoneNumber(String username, String email, String phoneNumber) {
        return memberRepository.findByUsernameAndEmailAndPhoneNumber(username, email, phoneNumber);
    }

    /**
     * 비밀번호 재설정
     */
    @Transactional
    /**
     * 비밀번호를 재설정합니다.
     *
     * @param username    사용자명
     * @param newPassword 새 비밀번호(평문; 인코딩 필요)
     */
    public void resetPassword(String username, String newPassword) {
        Member member = memberRepository.findByUsername(username);
        if (member != null) {
            member.setPassword(newPassword);
            memberRepository.save(member);
        }
    }
}
