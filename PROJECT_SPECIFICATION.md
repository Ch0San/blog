# 블로그 프로젝트 명세서


## 📋 프로젝트 개요

Spring Boot 기반의 블로그 시스템으로, 게시글 작성, 댓글, 좋아요, 동영상 콘텐츠(Stories) 등의 기능을 제공하는 웹 애플리케이션입니다.

---

## 🛠 기술 스택

### WAS (Web Application Server)
- **Apache Tomcat 10.1.48** (Embedded)
  - Spring Boot 3.5.7 내장
  - 기본 포트: 8080

### Database
- **MySQL 8.0** (Server)
  - 운영 데이터베이스
  - 포트: 3306
  - 데이터베이스명: `blog`
  - 문자셋: UTF-8 (utf8mb4)
  - Collation: utf8mb4_unicode_ci
  - JDBC Driver: `mysql-connector-j` (Runtime)
- **H2 Database**
  - 테스트용 인메모리 데이터베이스
  - Runtime scope

### Development Tools
- **Java**: 17
- **Maven**: 3.x (Maven Wrapper 포함)
- **Spring Boot DevTools**: 핫 리로드 지원
- **Lombok**: 보일러플레이트 코드 자동 생성
- **IDE**: VS Code / IntelliJ IDEA 권장

### Back-End Framework & Libraries

#### Core Framework
- **Spring Boot**: 3.5.7
- **Spring Framework**: 6.x (Spring Boot 포함)

#### Spring Modules
- **Spring Web (MVC)**
  - RESTful API 및 웹 컨트롤러
  - `@RestController`, `@Controller` 기반
  
- **Spring Data JPA**
  - Hibernate ORM
  - MySQL8Dialect
  - DDL Auto: `update`
  - Show SQL: `true`
  - Format SQL: `true`
  
- **Spring Security**
  - 인증/인가 처리
  - NoOpPasswordEncoder (평문 저장 - 개발용만)
  - Form 로그인
  - Remember-Me 기능
  - Security Debug 모드 활성화
  
- **Spring Validation**
  - Bean Validation (JSR-380)

#### Persistence
- **MyBatis Spring Boot Starter**: 3.0.5
  - SQL Mapper Framework
  - Type Aliases Package: `com.example.blog.domain`
  - Camel Case 자동 변환 활성화
  - Mapper Locations: `classpath:mapper/**/*.xml`
  - JDBC Type for NULL: `NULL`
  
- **HikariCP**
  - 커넥션 풀
  - Maximum Pool Size: 10
  - Minimum Idle: 5
  - Idle Timeout: 300000ms
  - Connection Timeout: 20000ms
  - Max Lifetime: 1200000ms

#### Database Driver
- **MySQL Connector/J**: Runtime
- **H2 Database**: Runtime (테스트용)

#### Utilities
- **Lombok**
  - `@Getter`, `@Setter`, `@NoArgsConstructor` 등
  - 컴파일 시 코드 생성
  - Annotation Processor 설정 포함

### Front-End

#### Template Engine
- **Thymeleaf**
  - Spring Boot Starter Thymeleaf
  - Thymeleaf Spring Security 6 통합

#### Static Resources
- 리소스 URL: `/css/**`, `/js/**`, `/images/**`, `/uploads/**`
- **CSS**: `/css/style.css`
- **JavaScript**: `/js/` (모듈화된 외부 스크립트)
  - `posts/detail.js` - 게시글 상세 (Kakao 지도, AJAX 좋아요)
  - `posts/edit.js` - 게시글 수정 (Kakao 지도)
  - `posts/write.js` - 게시글 작성 (Kakao 지도)
  - `stories/detail.js` - 스토리 AJAX 좋아요 및 댓글 좋아요
  - `stories/list.js` - 스토리 목록 동작 및 UI 보조
  - `stories/edit.js` - 스토리 수정
  - `stories/write.js` - 스토리 작성
- **Images**: `/images/`(정적), `/uploads/images/`(업로드)
- **Videos**: `/uploads/videos/`

#### UI Components
- HTML5
- CSS3
- JavaScript (Vanilla JS)

#### File Upload
- **Multipart Configuration**
  - Max File Size: 100MB
  - Max Request Size: 100MB

#### External API
- **Kakao Maps JavaScript API**
  - API Key: `kakao.maps.javascript.key` (application.properties에서 관리, 템플릿에 주입)
  - 게시글 작성/수정/상세 화면에서 지도 통합

---

## 📊 데이터베이스 명세서

### ERD 개요

블로그 시스템은 총 **13개의 테이블**로 구성되어 있으며, 회원, 게시글, 댓글, 좋아요, 동영상 콘텐츠, 공지사항, 방문자 통계, 사이트 설정을 관리합니다.

**테이블 목록:**
1. `members` - 회원 정보
2. `posts` - 게시글
3. `post_images` - 게시글 첨부 이미지
4. `post_likes` - 게시글 좋아요
5. `comments` - 게시글 댓글
6. `comment_likes` - 댓글 좋아요
7. `stories` - 동영상 콘텐츠 (Shorts)
8. `story_likes` - 스토리 좋아요
9. `story_comments` - 스토리 댓글
10. `story_comment_likes` - 스토리 댓글 좋아요
11. `notices` - 공지사항
12. `visitor_counts` - 방문자 통계
13. `site_settings` - 사이트 설정

---

### 1. **members** (회원 정보)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 회원 고유 식별자 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 로그인 아이디 |
| password | VARCHAR(255) | NOT NULL | 비밀번호 (평문 저장) |
| nickname | VARCHAR(50) | NOT NULL | 닉네임 |
| email | VARCHAR(100) | NOT NULL, UNIQUE | 이메일 주소 |
| phone_number | VARCHAR(50) | NULL | 전화번호 |
| address | VARCHAR(255) | NULL | 주소 |
| is_active | BOOLEAN | DEFAULT TRUE | 계정 활성화 여부 |
| last_login_at | DATETIME | NULL | 마지막 로그인 시간 |
| role | ENUM('USER', 'ADMIN') | NOT NULL, DEFAULT 'USER' | 사용자 권한 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 가입일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스:**
- `idx_username` (username)
- `idx_email` (email)

**비즈니스 규칙:**
- username과 email은 유일해야 함
- 기본 권한은 USER, 관리자는 ADMIN
- 초기 관리자 계정: admin / 1111 (DataInitializer에서 자동 생성)

---

### 2. **posts** (게시글)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 게시글 고유 식별자 |
| title | VARCHAR(255) | NOT NULL | 게시글 제목 |
| content | TEXT | NULL | 게시글 본문 내용 |
| author | VARCHAR(100) | NOT NULL | 작성자 이름 |
| view_count | BIGINT | DEFAULT 0 | 조회수 |
| like_count | BIGINT | DEFAULT 0 | 좋아요 수 |
| is_public | BOOLEAN | DEFAULT TRUE | 공개 여부 |
| thumbnail_url | VARCHAR(500) | NULL | 썸네일 이미지 URL |
| video_url | VARCHAR(500) | NULL | 동영상 URL |
| files_url | TEXT | NULL | 첨부파일경로 URL |
| category | VARCHAR(50) | NULL | 카테고리 |
| tags | VARCHAR(500) | NULL | 태그 (쉼표 구분) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 작성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스:**
- `idx_author` (author)
- `idx_category` (category)
- `idx_created_at` (created_at)
- `idx_fulltext_title_content` FULLTEXT (title, content)

**비즈니스 규칙:**
- ADMIN 권한만 작성/수정/삭제 가능
- 조회는 모든 사용자 가능 (is_public=TRUE인 경우)
- 다중 이미지는 post_images 테이블에서 관리
- 게시글 삭제 시 본문에 포함된 이미지/동영상 파일도 함께 삭제

---

### 3. **post_images** (게시글 첨부 이미지)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 이미지 고유 식별자 |
| post_id | BIGINT | NOT NULL, FK | 게시글 ID |
| image_url | VARCHAR(500) | NOT NULL | 이미지 URL |
| sort_order | INT | NULL | 정렬 순서 |
| caption | VARCHAR(255) | NULL | 이미지 캡션 |

**외래키:**
- `post_id` → posts(id) ON DELETE CASCADE

**인덱스:**
- `idx_post_images_post_id` (post_id)
- `idx_post_images_sort` (sort_order)

**비즈니스 규칙:**
- 하나의 게시글에 여러 이미지 첨부 가능
- 게시글 삭제 시 이미지도 함께 삭제 (CASCADE)
- sort_order로 이미지 순서 관리

---

### 4. **post_likes** (게시글 좋아요)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 좋아요 고유 식별자 |
| post_id | BIGINT | NOT NULL, FK | 게시글 ID |
| username | VARCHAR(100) | NOT NULL | 좋아요를 누른 사용자 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 좋아요 누른 시간 |

**외래키:**
- `post_id` → posts(id) ON DELETE CASCADE

**제약조건:**
- UNIQUE(post_id, username) - 한 사용자는 게시글당 1회만 좋아요 가능

**인덱스:**
- `idx_post_likes_post_id` (post_id)

**비즈니스 규칙:**
- 인증된 사용자만 좋아요 가능
- 중복 좋아요 방지
- AJAX 방식으로 처리하여 조회수 증가 방지

---

### 5. **comments** (게시글 댓글)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 댓글 고유 식별자 |
| content | TEXT | NOT NULL | 댓글 내용 |
| author | VARCHAR(100) | NOT NULL | 댓글 작성자 (표시용 닉네임) |
| author_username | VARCHAR(100) | NULL | 작성자 로그인 아이디 |
| like_count | BIGINT | DEFAULT 0 | 좋아요 수 |
| post_id | BIGINT | NOT NULL, FK | 댓글이 속한 게시글 ID |
| parent_id | BIGINT | NULL, FK | 부모 댓글 ID (대댓글) |
| is_deleted | BOOLEAN | DEFAULT FALSE | 소프트 삭제 여부 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 작성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**외래키:**
- `post_id` → posts(id) ON DELETE CASCADE
- `parent_id` → comments(id) ON DELETE CASCADE

**인덱스:**
- `idx_post_id` (post_id)
- `idx_parent_id` (parent_id)
- `idx_created_at` (created_at)

**비즈니스 규칙:**
- 계층형 댓글 구조 지원 (대댓글)
- 삭제 시 소프트 삭제 (is_deleted=TRUE)
- 게시글 삭제 시 댓글도 함께 삭제 (CASCADE)
- 댓글이 없을 때 "첫 댓글을 작성해보세요!" 메시지 표시

---

### 6. **comment_likes** (댓글 좋아요)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 좋아요 고유 식별자 |
| comment_id | BIGINT | NOT NULL, FK | 댓글 ID |
| username | VARCHAR(100) | NOT NULL | 좋아요를 누른 사용자 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 좋아요 누른 시간 |

**외래키:**
- `comment_id` → comments(id) ON DELETE CASCADE

**제약조건:**
- UNIQUE(comment_id, username) - 한 사용자는 댓글당 1회만 좋아요 가능

**인덱스:**
- `idx_comment_id` (comment_id)

**비즈니스 규칙:**
- 인증된 사용자만 좋아요 가능
- 중복 좋아요 방지
- AJAX 방식으로 처리하여 조회수 증가 방지

---

### 7. **stories** (동영상 콘텐츠)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | Stories 고유 식별자 |
| title | VARCHAR(255) | NOT NULL | 동영상 제목 |
| description | TEXT | NULL | 동영상 설명 |
| author | VARCHAR(100) | NOT NULL | 작성자 이름 |
| video_url | VARCHAR(500) | NOT NULL | 동영상 URL (필수) |
| thumbnail_url | VARCHAR(500) | NULL | 썸네일 이미지 URL |
| view_count | BIGINT | DEFAULT 0 | 조회수 |
| like_count | BIGINT | DEFAULT 0 | 좋아요 수 |
| category | VARCHAR(50) | NULL | 카테고리 (여행, 맛집, 기술, 일상, 브이로그 등) |
| tags | VARCHAR(500) | NULL | 태그 (쉼표 구분) |
| duration | INT | DEFAULT 0 | 동영상 길이(초) |
| is_public | BOOLEAN | DEFAULT TRUE | 공개 여부 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 작성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스:**
- `idx_author` (author)
- `idx_category` (category)
- `idx_created_at` (created_at)
- `idx_view_count` (view_count)

**비즈니스 규칙:**
- ADMIN 권한만 작성/수정/삭제 가능
- 조회는 모든 사용자 가능 (is_public=TRUE인 경우)
- 동영상만 업로드 가능 (제목, 설명, 100MB 이하 동영상)
- 썸네일은 동영상 첫 프레임 자동 생성 (HTML5 video#t=0.1)
- 리스트에서 마우스 호버 2초 시 0~5초 구간 미리보기 재생
- 스토리 삭제 시 video_url, thumbnail_url 파일도 함께 삭제
- 상세 페이지에서 동영상 플레이어 중앙 정렬 (flexbox)

---

### 8. **story_likes** (스토리 좋아요)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 좋아요 고유 식별자 |
| story_id | BIGINT | NOT NULL, FK | 스토리 ID |
| username | VARCHAR(100) | NOT NULL | 좋아요를 누른 사용자 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 좋아요 누른 시간 |

**외래키:**
- `story_id` → stories(id) ON DELETE CASCADE

**제약조건:**
- UNIQUE(story_id, username) - 한 사용자는 스토리당 1회만 좋아요 가능

**인덱스:**
- `idx_story_likes_story_id` (story_id)

**비즈니스 규칙:**
- 인증된 사용자만 좋아요 가능
- 중복 좋아요 방지
- AJAX 방식으로 처리하여 조회수 증가 방지

---

### 9. **story_comments** (스토리 댓글)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 스토리 댓글 고유 식별자 |
| content | TEXT | NOT NULL | 댓글 내용 |
| author | VARCHAR(100) | NOT NULL | 댓글 작성자 (표시용 닉네임) |
| author_username | VARCHAR(100) | NOT NULL | 작성자 로그인 아이디 |
| story_id | BIGINT | NOT NULL, FK | 스토리 ID |
| like_count | BIGINT | DEFAULT 0 | 댓글 좋아요 수 |
| is_deleted | BOOLEAN | DEFAULT FALSE | 댓글 삭제 여부 (소프트 삭제) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 작성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**외래키:**
- `story_id` → stories(id) ON DELETE CASCADE

**인덱스:**
- `idx_story_id` (story_id)
- `idx_story_created_at` (created_at)

**비즈니스 규칙:**
- 인증된 사용자만 댓글 작성 가능
- 삭제 시 소프트 삭제 (is_deleted=TRUE)
- 스토리 삭제 시 댓글도 함께 삭제 (CASCADE)
- 댓글이 없을 때 "첫 댓글을 작성해보세요!" 메시지 표시

---

### 10. **story_comment_likes** (스토리 댓글 좋아요)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 좋아요 고유 식별자 |
| comment_id | BIGINT | NOT NULL, FK | 스토리 댓글 ID |
| username | VARCHAR(100) | NOT NULL | 좋아요를 누른 사용자 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 좋아요 누른 시간 |

**외래키:**
- `comment_id` → story_comments(id) ON DELETE CASCADE

**제약조건:**
- UNIQUE(comment_id, username) - 한 사용자는 댓글당 1회만 좋아요 가능

**인덱스:**
- `idx_story_comment_id` (comment_id)

**비즈니스 규칙:**
- 인증된 사용자만 좋아요 가능
- 중복 좋아요 방지
- AJAX 방식으로 처리하여 조회수 증가 방지

---

### 11. **visitor_counts** (방문자 통계)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 방문자 카운트 고유 식별자 |
| visit_date | DATE | NOT NULL, UNIQUE | 방문 날짜 (일별 집계) |
| count | BIGINT | NOT NULL, DEFAULT 0 | 해당 날짜 방문자 수 |

**인덱스:**
- `idx_visit_date` (visit_date)

**비즈니스 규칙:**
- 일별 방문자 통계
- 날짜별 유일한 레코드

---

### 12. **site_settings** (사이트 설정)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 설정 고유 식별자 |
| setting_key | VARCHAR(100) | NOT NULL, UNIQUE | 설정 키 |
| setting_value | TEXT | NULL | 설정 값 |
| description | TEXT | NULL | 설정 설명 |

**인덱스:**
- `idx_setting_key` (setting_key)

**비즈니스 규칙:**
- Key-Value 형태의 사이트 설정 저장
- 예: `site_tags` → "Java,Spring,MyBatis"
- 예: `site_introduction` → 홈 소개 문구
- 예: `site_hero_image_url` → 메인 히어로 이미지 URL

---

### 13. **notices** (공지사항)

| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|------------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 공지사항 고유 식별자 |
| title | VARCHAR(255) | NOT NULL | 공지 제목 |
| content | TEXT | NOT NULL | 공지 본문 내용 |
| author | VARCHAR(100) | NOT NULL | 작성자 이름 |
| view_count | BIGINT | DEFAULT 0 | 조회수 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 작성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스:**
- `idx_notices_created_at` (created_at)

**비즈니스 규칙:**
- ADMIN 권한만 작성/수정/삭제 가능
- 조회는 모든 사용자 가능
- 댓글/좋아요/태그/지도/이미지 업로드 기능 없음 (텍스트 중심)
- 상세 조회 시 조회수 증가

---

## 🔐 보안 설정

### Spring Security 구성

#### 인증 방식
- **Form Login**
  - 로그인 페이지: `/member/signin`
  - 로그인 처리: POST `/member/signin`
  - 성공 시: `/`로 리다이렉트
  - 실패 시: `/member/signin?error`
  - 로그아웃: `/member/signout` (성공 시 `/`)

#### 비밀번호 인코더
- **NoOpPasswordEncoder** (평문 저장)
  - ⚠️ **운영 환경 사용 금지**
  - 개발/학습 목적으로만 사용

#### 권한 설정
| URL 패턴 | 접근 권한 |
|---------|----------|
| `/`, `/index.html` | permitAll |
| `/css/**`, `/js/**`, `/images/**`, `/webjars/**`, `/uploads/**` | permitAll |
| `/member/signup`, `/member/signin`, `/member/find-id`, `/member/find-password`, `/member/reset-password` | permitAll |
| GET `/notice`, `/notice/{id}` | permitAll |
| GET `/stories`, `/stories/{id}`, `/stories/search/**` | permitAll |
| GET `/posts`, `/posts/{id}`, `/posts/search/**`, `/posts/download`, `/api/posts/**` | permitAll |
| `/posts/write`, `/posts/edit/**`, `/posts/delete/**` | ADMIN만 |
| `/stories/write`, `/stories/edit/**`, `/stories/delete/**` | ADMIN만 |
| `/notice/write`, `/notice/edit/**`, `/notice/delete/**` | ADMIN만 |
| `/member/update`, `/member/delete` | 인증 필요 |
| `/member/list`, `/member/admin/**`, `/member/tag-update`, `/admin/**` | ADMIN만 |
| POST `/posts/*/comments`, `/posts/*/like`, `/comments/**`, `/replies/**` | 인증 필요 |

#### Remember-Me
- 활성화: 14일
- Key: `blog-remember-me-key`

---

## 📁 프로젝트 구조

```
blog/
├── src/
│   ├── main/
│   │   ├── java/com/example/blog/
│   │   │   ├── BlogApplication.java              # 메인 클래스
│   │   │   ├── config/                           # 설정
│   │   │   │   ├── DataInitializer.java         # 초기 데이터 (admin 계정)
│   │   │   │   ├── SecurityConfig.java          # 보안 설정
│   │   │   │   ├── WebConfig.java               # 웹 설정 (정적 리소스)
│   │   │   │   └── GlobalModelAttributes.java   # 전역 속성
│   │   │   ├── controller/                       # 컨트롤러
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── NoticeController.java
│   │   │   │   ├── MemberController.java
│   │   │   │   ├── PostController.java
│   │   │   │   ├── PostLikeController.java
│   │   │   │   ├── CommentController.java
│   │   │   │   ├── CommentLikeController.java
│   │   │   │   ├── StoriesController.java
│   │   │   │   ├── StoryLikeController.java
│   │   │   │   ├── StoryCommentLikeController.java
│   │   │   │   ├── UploadController.java
│   │   │   │   └── CustomErrorController.java
│   │   │   │   ├── AdminSiteController.java
│   │   │   │   └── AdminDebugController.java
│   │   │   ├── domain/                           # 엔티티
│   │   │   │   ├── Member.java
│   │   │   │   ├── Post.java
│   │   │   │   ├── PostImage.java
│   │   │   │   ├── PostLike.java
│   │   │   │   ├── Comment.java
│   │   │   │   ├── CommentLike.java
│   │   │   │   ├── Story.java
│   │   │   │   ├── StoryLike.java
│   │   │   │   ├── StoryComment.java
│   │   │   │   ├── StoryCommentLike.java
│   │   │   │   ├── Notice.java
│   │   │   │   ├── VisitorCount.java
│   │   │   │   ├── SiteSetting.java
│   │   │   │   └── Role.java (ENUM)
│   │   │   ├── repository/                       # JPA 레포지토리
│   │   │   │   ├── MemberRepository.java
│   │   │   │   ├── PostRepository.java
│   │   │   │   ├── PostLikeRepository.java
│   │   │   │   ├── CommentRepository.java
│   │   │   │   ├── CommentLikeRepository.java
│   │   │   │   ├── StoryRepository.java
│   │   │   │   ├── StoryLikeRepository.java
│   │   │   │   ├── StoryCommentRepository.java
│   │   │   │   ├── StoryCommentLikeRepository.java
│   │   │   │   ├── NoticeRepository.java
│   │   │   │   ├── VisitorCountRepository.java
│   │   │   │   └── SiteSettingRepository.java
│   │   │   └── service/                          # 서비스 계층
│   │   │       ├── MemberService.java
│   │   │       ├── CustomUserDetailsService.java
│   │   │       ├── PostService.java
│   │   │       ├── PostLikeService.java
│   │   │       ├── CommentService.java
│   │   │       ├── CommentLikeService.java
│   │   │       ├── StoryService.java
│   │   │       ├── StoryLikeService.java
│   │   │       ├── StoryCommentService.java
│   │   │       ├── StoryCommentLikeService.java
│   │   │       ├── NoticeService.java
│   │   │       ├── VisitorCountService.java
│   │   │       └── SiteSettingService.java
│   │   └── resources/
│   │       ├── application.properties            # 설정 파일
│   │       ├── static/                           # 정적 리소스
│   │       │   ├── css/
│   │       │   │   └── style.css
│   │       │   ├── images/                       # 정적 이미지
│   │       │   ├── js/                           # JavaScript 모듈
│   │       │   │   ├── posts/
│   │       │   │   │   ├── detail.js            # 게시글 상세 (지도, 좋아요)
│   │       │   │   │   ├── edit.js              # 게시글 수정 (지도)
│   │       │   │   │   └── write.js             # 게시글 작성 (지도)
│   │       │   │   └── stories/
│   │       │   │       ├── list.js              # 스토리 목록 동작
│   │       │   │       ├── detail.js            # 스토리 좋아요 AJAX
│   │       │   │       ├── edit.js              # 스토리 수정
│   │       │   │       └── write.js             # 스토리 작성
│   │       │   └── videos/                       # 업로드된 동영상
│   │       └── templates/                        # Thymeleaf 템플릿
│   │           ├── index.html
│   │           ├── mainImageEdit.html            # 관리자 메인 히어로 이미지 관리
│   │           ├── fragments/
│   │           │   ├── header.html
│   │           │   ├── footer.html
│   │           │   ├── index_403.html
│   │           │   ├── index_404.html
│   │           │   └── index_500.html
│   │           ├── member/
│   │           │   ├── signUp.html
│   │           │   ├── signIn.html
│   │           │   ├── memberUpdate.html
│   │           │   ├── adminEdit.html
│   │           │   ├── list.html
│   │           │   ├── tagUpdate.html
│   │           │   ├── introductionUpdate.html
│   │           │   ├── findId.html
│   │           │   └── findPassword.html
│   │           ├── posts/
│   │           │   ├── list.html
│   │           │   ├── detail.html
│   │           │   ├── write.html
│   │           │   └── edit.html
│   │           ├── notice/
│   │           │   ├── list.html
│   │           │   ├── detail.html
│   │           │   ├── write.html
│   │           │   └── edit.html
│   │           └── stories/
│   │               ├── list.html
│   │               ├── detail.html
│   │               ├── write.html                # 동영상 전용 업로드
│   │               └── edit.html                 # 동영상 전용 수정
│   └── test/
│       └── java/com/example/blog/
│           └── BlogApplicationTests.java
├── uploads/                                      # 업로드 파일 저장소
│   ├── images/                                   # 이미지 파일
│   │   ├── test_icon_01.png ~ test_icon_20.png  # 테스트용 이미지
│   └── videos/                                   # 동영상 파일
├── target/                                       # Maven 빌드 결과
├── pom.xml                                       # Maven 설정
├── create_tables.sql                             # DDL 스크립트
├── test_data.sql                                 # 테스트 데이터
├── mvnw                                          # Maven Wrapper (Unix)
├── mvnw.cmd                                      # Maven Wrapper (Windows)
└── PROJECT_SPECIFICATION.md                      # 프로젝트 명세서 (이 파일)
```

---

## 🚀 실행 방법

### 1. 데이터베이스 설정
```sql
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 테이블 생성
```bash
# MySQL 접속 후
mysql -u root -p blog < create_tables.sql

# (선택) 테스트 데이터 삽입
mysql -u root -p blog < test_data.sql
```

### 3. application.properties 확인
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blog?serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=1111
```

### 4. 애플리케이션 실행
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 5. 접속
- **URL**: http://localhost:8080
- **관리자 계정**: `admin` / `1111` (자동 생성)

---

## 🎯 주요 기능

### 회원 관리
- 회원가입 / 로그인 / 로그아웃
- 회원 정보 수정 / 탈퇴
- 관리자 권한 관리 (태그 수정, 회원 목록)
- Remember-Me (자동 로그인)

### 게시글 관리 (Posts)
- 게시글 목록 조회 (페이징, 카테고리 필터)
- 게시글 상세 조회 (Kakao 지도 API 통합)
- 게시글 작성 / 수정 / 삭제 (ADMIN만)
- 다중 이미지 업로드 및 에디터 내 삽입
- 조회수 / 좋아요 기능 (AJAX 방식)
- 카테고리 / 태그 분류
- 댓글 / 대댓글 / 댓글 좋아요
- Kakao Maps JavaScript API 통합

### 스토리 관리 (Stories)
- 동영상 목록 조회 (카드 레이아웃)
- 동영상 상세 조회 (중앙 정렬 플레이어)
- 동영상 작성 / 수정 / 삭제 (ADMIN만)
  - 제목, 설명, 100MB 이하 동영상 업로드
  - 썸네일 자동 생성 (첫 프레임)
  - 메타데이터 (카테고리, 태그, 길이)
- 리스트 호버 미리보기 (1초 지연, 0~5초 재생)
- 댓글 / 좋아요 기능 (AJAX 방식)
- 조회수 카운팅

### 공지사항 (Notice)
- 공지사항 목록/상세 조회 (공개)
- 공지사항 작성 / 수정 / 삭제 (ADMIN만)
- 댓글/좋아요/태그/지도/미디어 없음 (텍스트 중심)
- 상세 조회 시 조회수 증가
- 홈(index) 화면 우측 위젯에 최근 공지 5개 노출

### 댓글 기능
- 댓글 작성 / 수정 / 삭제
- 대댓글 (계층형 구조, Posts만)
- 댓글 좋아요 (AJAX 방식)
- 소프트 삭제
- "첫 댓글을 작성해보세요!" 메시지

### 파일 관리
- 게시글/스토리 삭제 시 관련 파일 자동 삭제
  - 게시글: 썸네일, 동영상, 본문 내 이미지
  - 스토리: 동영상, 썸네일
- 패턴 매칭 기반 파일 검색 및 제거
  - Regex: `/uploads/(images|videos)/[^"'\\s)<>]+`

### 통계 및 설정
- 일별 방문자 통계
- 사이트 태그 관리 (ADMIN)
- 전역 모델 속성 (사이트 설정)

---

## ⚠️ 주의사항

### 1. 보안 이슈
- **비밀번호 평문 저장**
  - 현재 `NoOpPasswordEncoder` 사용 (평문 저장)
  - ⚠️ **운영 환경에서는 절대 사용 금지**
  - 개발/학습 목적으로만 사용
  - 운영 시 `BCryptPasswordEncoder` 사용 권장

### 2. Hibernate 설정
- **MySQL8Dialect**
  - Hibernate 6.x에서는 자동으로 `MySQLDialect` 감지
  - 명시적 설정 제거 가능
- **DDL Auto: update**
  - 운영 환경에서는 `validate` 또는 `none` 사용 권장

### 3. JPA Open-in-View
- 기본값 `true`로 설정
- 성능 이슈 발생 시 `false`로 변경 고려
- Lazy Loading 전략 변경 필요

### 4. 파일 업로드
- Max File Size: 100MB (Stories 동영상)
- 업로드 디렉토리: `/uploads/images/`, `/uploads/videos/`
- 파일 검증 로직 필요 (확장자, MIME 타입)

### 5. Kakao Maps API
- API Key는 `application.properties`의 `kakao.maps.javascript.key`로 관리
- 템플릿과 JS에서 주입/로딩하여 사용
- API 사용량 제한 모니터링 필요

---

## 📝 개발 시 고려사항

### 1. 추가 구현 권장 사항
- [ ] 비밀번호 암호화 (BCrypt)
- [ ] CSRF 토큰 활성화
- [ ] XSS 방어 강화
- [ ] SQL Injection 방어
- [ ] 파일 업로드 검증 강화
- [ ] API Rate Limiting
- [ ] 에러 페이지 커스터마이징
- [ ] 로깅 전략 수립
- [ ] 동영상 인코딩/스트리밍 최적화
- [ ] CDN 통합 (정적 리소스, 동영상)

### 2. 성능 최적화
- [ ] 쿼리 최적화 (N+1 문제)
- [ ] 캐싱 전략 (Redis) - 조회수, 좋아요 수
- [ ] 정적 리소스 CDN 활용
- [ ] DB 인덱스 최적화
- [ ] Lazy Loading 전략
- [ ] 동영상 썸네일 사전 생성
- [ ] 페이징 성능 개선

### 3. 테스트
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] E2E 테스트 작성
- [ ] 테스트 커버리지 향상
- [ ] 파일 업로드/삭제 테스트
- [ ] AJAX 엔드포인트 테스트

### 4. 기능 개선
- [ ] 동영상 스트리밍 지원 (HLS/DASH)
- [ ] 이미지 리사이징 및 최적화
- [ ] 검색 기능 강화 (Elasticsearch)
- [ ] 알림 기능 (댓글, 좋아요)
- [ ] 소셜 로그인 (OAuth2)
- [ ] 반응형 웹 디자인 개선
- [ ] 다크 모드 지원

---

## 🔄 주요 변경 이력

### 스크립트 외부화
- 모든 인라인 스크립트를 `/static/js/` 폴더로 분리
- `posts/detail.js`, `posts/edit.js`, `posts/write.js` - Kakao Maps API 통합
- `stories/detail.js` - AJAX 좋아요 및 댓글 좋아요

### 스토리 동영상 전용화
- `stories/write.html`, `stories/edit.html` 간소화
- 제목, 설명, 100MB 이하 동영상만 업로드
- 썸네일 자동 생성 (HTML5 video#t=0.1)

### 동영상 기능 강화
- 호버 미리보기 (2초 지연, 0~5초 재생)
- 동영상 플레이어 중앙 정렬 (flexbox)
- max-width, max-height 제약

### 댓글 UI 개선
- "첫 댓글을 작성해보세요!" 메시지 추가
- 댓글 폼 위에 배치

### 파일 삭제 자동화
- 게시글/스토리 삭제 시 관련 파일 자동 제거
- 패턴 매칭으로 본문 내 이미지 검색

### 테스트 데이터
- 공지사항 13개 샘플 추가
- 20개 게시글로 확장
- 로컬 이미지 사용 (test_icon_01.png ~ test_icon_20.png)

### AJAX 좋아요 시스템
- 게시글 및 스토리 좋아요 AJAX 처리
- 조회수 증가 방지
- JSON 응답 ({isLiked, likeCount})

### 데이터베이스 스키마
- notices 테이블 추가
- story_likes 테이블 추가
- UNIQUE 제약조건 및 인덱스 최적화

---

## 📝 라이선스
이 프로젝트는 학습 목적으로 작성되었습니다.

---

## 👨‍💻 개발자
- **GitHub**: Ch0San
- **Repository**: blog
- **Branch**: main
- **개발 기간**: 2025년

---

## 📚 참고 자료
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Thymeleaf Documentation](https://www.thymeleaf.org/)
- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
- [Kakao Maps API](https://apis.map.kakao.com/)
