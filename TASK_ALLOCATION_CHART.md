# 작업 할당 표 (Task Allocation Chart)

## 작업 분류 (Category)
- DevOps: 빌드/배포/모니터링/인프라
- Backend: API, 비즈니스 로직, 보안, 데이터 접근
- Frontend: 템플릿/JS/CSS/UX
- DB: 모델링/DDL/마이그레이션/성능
- QA: 테스트/시나리오/품질 보증

## 작업 표
| ID | 작업 | 범주 | 담당자 | 역할 | 기간(시작–종료) | 상태 | 우선순위 | 의존성 | 산출물 |
|----|------|------|--------|------|------------------|------|----------|--------|--------|
| T01 | 프로젝트 초기 세팅 (Spring Boot, Maven Wrapper, DevTools) | DevOps | TBD | DevOps | TBD–TBD | Not Started | High | 없음 | 빌드 가능 스캐폴드 |
| T02 | DB 설계: ERD 확정 및 스키마 생성 | DB | TBD | DBA | TBD–TBD | Not Started | High | T01 | `create_tables.sql`, ERD 이미지 |
| T03 | JPA/MyBatis 설정 및 리포지토리 베이스 구성 | Backend | TBD | Backend | TBD–TBD | Not Started | High | T02 | JPA 엔티티, 매퍼, 설정 |
| T04 | Spring Security 인증/인가 (폼 로그인, Remember-Me) | Backend | TBD | Backend | TBD–TBD | Not Started | High | T01 | `SecurityConfig`, 로그인/권한 흐름 |
| T05 | 회원 도메인 (`members`) CRUD 및 밸리데이션 | Backend | TBD | Backend | TBD–TBD | Not Started | Medium | T03,T04 | API/화면, 단위 테스트 |
| T06 | 게시글 도메인 (`posts`, `post_images`, `post_likes`) | Backend | TBD | Backend | TBD–TBD | Not Started | High | T03 | 게시글 CRUD, 좋아요, 이미지 업로드 |
| T07 | 댓글 도메인 (`comments`, `comment_likes`) | Backend | TBD | Backend | TBD–TBD | Not Started | Medium | T06 | 댓글 CRUD/좋아요 API |
| T08 | 스토리(Shorts) 도메인 (`stories`, `story_*`) | Backend | TBD | Backend | TBD–TBD | Not Started | Medium | T03 | 스토리 CRUD/좋아요/댓글 API |
| T09 | 공지사항 도메인 (`notices`) 및 홈 노출 | Backend | TBD | Backend | TBD–TBD | Not Started | Medium | T03 | 공지 API/템플릿/권한 |
| T10 | 파일 업로드 (이미지/비디오) 및 용량 제한 | Backend | TBD | Backend | TBD–TBD | Not Started | Medium | T06 | 업로드 엔드포인트/저장소 정책 |
| T11 | Kakao Maps JS 연동 (글 작성/수정/상세) | Frontend | TBD | Frontend | TBD–TBD | Not Started | Medium | T06 | 지도 위젯/키 주입/이벤트 |
| T12 | 템플릿(Thymeleaf) 및 레이아웃(헤더/사이드바/카드) | Frontend | TBD | Frontend | TBD–TBD | Not Started | High | T06,T09 | `templates/**`, 공통 레이아웃 |
| T13 | 정적 리소스 구조 정리 (`/css`, `/js`, `/images`, `/uploads`) | Frontend | TBD | Frontend | TBD–TBD | Not Started | Medium | T12 | 스타일 가이드/번들 구조 |
| T14 | 검색/정렬/페이징: 게시글 목록 UX | Frontend | TBD | Frontend | TBD–TBD | Not Started | Medium | T06 | 목록 UI/쿼리 파라미터 |
| T15 | 테스트 데이터 및 마이그레이션 스크립트 | DB | TBD | DBA | TBD–TBD | Not Started | Medium | T02 | `test_data.sql`, 초기화 절차 |
| T16 | 모니터링/로깅/SQL 포맷/디버그 설정 | DevOps | TBD | DevOps | TBD–TBD | Not Started | Medium | T01 | 로그 정책, SQL 포맷 |
| T17 | 성능 점검(인덱스, 풀링, N+1) | DB | TBD | DBA | TBD–TBD | Not Started | Low | T03,T06 | 인덱스/쿼리 튜닝 리포트 |
| T18 | 접근성/반응형/크로스브라우저 점검 | Frontend | TBD | Frontend | TBD–TBD | Not Started | Low | T12 | 개선 체크리스트 |
| T19 | 통합 테스트/QA 시나리오/버그 트래킹 | QA | TBD | QA | TBD–TBD | Not Started | High | T05–T14 | 테스트 케이스, 버그 리포트 |
| T20 | 배포 파이프라인(CI/CD) 및 환경 구성 | DevOps | TBD | DevOps | TBD–TBD | Not Started | High | T01 | CI/CD 파이프라인, 프로필 분리 |
