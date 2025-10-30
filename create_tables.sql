CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog;

-- ============================================
CREATE TABLE IF NOT EXISTS members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '회원 고유 식별자',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '사용자 아이디 (로그인용)',
    password VARCHAR(255) NOT NULL COMMENT '비밀번호',
    nickname VARCHAR(50) NOT NULL COMMENT '닉네임',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일',
    phone_number VARCHAR(50) COMMENT '전화번호',
    address VARCHAR(255) COMMENT '주소',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 활성화 여부',
    last_login_at DATETIME NULL COMMENT '마지막 로그인 일시',
    role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER' COMMENT '사용자 권한',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원 정보 테이블';

-- ============================================
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시글 고유 식별자',
    title VARCHAR(255) NOT NULL COMMENT '게시글 제목',
    content TEXT COMMENT '게시글 본문 내용',
    author VARCHAR(100) NOT NULL COMMENT '작성자 이름',
    view_count BIGINT DEFAULT 0 COMMENT '조회수',
    like_count BIGINT DEFAULT 0 COMMENT '좋아요 수',
    is_public BOOLEAN DEFAULT TRUE COMMENT '게시글 공개 여부',
    thumbnail_url VARCHAR(500) COMMENT '썸네일 이미지 URL',
    video_url VARCHAR(500) COMMENT '동영상 URL',
    category VARCHAR(50) COMMENT '게시글 카테고리',
    tags VARCHAR(500) COMMENT '게시글 태그 (쉼표로 구분)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    INDEX idx_author (author),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at),
    FULLTEXT INDEX idx_fulltext_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 테이블';

-- ============================================
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '댓글 고유 식별자',
    content TEXT NOT NULL COMMENT '댓글 내용',
    author VARCHAR(100) NOT NULL COMMENT '댓글 작성자',
    author_username VARCHAR(100) COMMENT '댓글 작성자 로그인 아이디',
    like_count BIGINT DEFAULT 0 COMMENT '댓글 좋아요 수',
    post_id BIGINT NOT NULL COMMENT '댓글이 속한 게시글 ID',
    parent_id BIGINT COMMENT '부모 댓글 ID (대댓글인 경우)',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '댓글 삭제 여부 (소프트 삭제)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='댓글 테이블';

-- ============================================
-- 3-1. 댓글 좋아요 (Comment_Likes)
-- ============================================
CREATE TABLE IF NOT EXISTS comment_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '댓글 좋아요 고유 식별자',
    comment_id BIGINT NOT NULL COMMENT '댓글 ID',
    username VARCHAR(100) NOT NULL COMMENT '사용자 아이디',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    CONSTRAINT uq_comment_likes UNIQUE (comment_id, username),
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_comment_id (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='댓글 좋아요 테이블';

-- ============================================
CREATE TABLE IF NOT EXISTS visitor_counts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '방문자 카운트 고유 식별자',
    visit_date DATE UNIQUE NOT NULL COMMENT '방문 날짜 (일별 집계)',
    count BIGINT NOT NULL DEFAULT 0 COMMENT '해당 날짜의 방문자 수',
    INDEX idx_visit_date (visit_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일별 방문자 수 테이블';

-- ============================================
CREATE TABLE IF NOT EXISTS stories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Shorts 고유 식별자',
    title VARCHAR(255) NOT NULL COMMENT '동영상 제목',
    description TEXT COMMENT '동영상 설명',
    author VARCHAR(100) NOT NULL COMMENT '작성자 이름',
    video_url VARCHAR(500) NOT NULL COMMENT '동영상 URL (필수)',
    thumbnail_url VARCHAR(500) COMMENT '썸네일 이미지 URL',
    view_count BIGINT DEFAULT 0 COMMENT '조회수',
    like_count BIGINT DEFAULT 0 COMMENT '좋아요 수',
    category VARCHAR(50) COMMENT '카테고리 (여행, 맛집, 기술, 일상, 브이로그 등)',
    tags VARCHAR(500) COMMENT '태그 (쉼표로 구분)',
    duration INT DEFAULT 0 COMMENT '동영상 길이(초)',
    is_public BOOLEAN DEFAULT TRUE COMMENT '공개 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    INDEX idx_author (author),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at),
    INDEX idx_view_count (view_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Shorts(동영상) 콘텐츠 테이블';

-- ============================================
-- 5-1. 스토리 댓글 (Story_Comments)
-- ============================================
CREATE TABLE IF NOT EXISTS story_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '스토리 댓글 고유 식별자',
    content TEXT NOT NULL COMMENT '댓글 내용',
    author VARCHAR(100) NOT NULL COMMENT '댓글 작성자',
    author_username VARCHAR(100) NOT NULL COMMENT '댓글 작성자 로그인 아이디',
    story_id BIGINT NOT NULL COMMENT '스토리 ID',
    like_count BIGINT DEFAULT 0 COMMENT '댓글 좋아요 수',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '댓글 삭제 여부 (소프트 삭제)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    INDEX idx_story_id (story_id),
    INDEX idx_story_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스토리 댓글 테이블';

-- ============================================
-- 5-2. 스토리 댓글 좋아요 (Story_Comment_Likes)
-- ============================================
CREATE TABLE IF NOT EXISTS story_comment_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '스토리 댓글 좋아요 고유 식별자',
    comment_id BIGINT NOT NULL COMMENT '스토리 댓글 ID',
    username VARCHAR(100) NOT NULL COMMENT '사용자 아이디',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    CONSTRAINT uq_story_comment_likes UNIQUE (comment_id, username),
    FOREIGN KEY (comment_id) REFERENCES story_comments(id) ON DELETE CASCADE,
    INDEX idx_story_comment_id (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스토리 댓글 좋아요 테이블';

-- ============================================
CREATE TABLE IF NOT EXISTS site_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '설정 고유 식별자',
    setting_key VARCHAR(100) UNIQUE NOT NULL COMMENT '설정 키',
    setting_value TEXT COMMENT '설정 값',
    description TEXT COMMENT '설정 설명',
    INDEX idx_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사이트 설정 테이블';

-- ============================================
-- 공지사항 (Notices)
-- 댓글/좋아요/미디어 없이 단순 공지용 게시판
-- ============================================
CREATE TABLE IF NOT EXISTS notices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '공지 고유 식별자',
    title VARCHAR(255) NOT NULL COMMENT '공지 제목',
    content TEXT NOT NULL COMMENT '공지 내용',
    author VARCHAR(100) NOT NULL COMMENT '작성자(표시용)',
    view_count BIGINT DEFAULT 0 COMMENT '조회수',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    INDEX idx_notices_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공지사항 테이블';

-- ============================================
-- 7. 게시글 이미지 (Post_Images)
-- ============================================
CREATE TABLE IF NOT EXISTS post_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시글 이미지 고유 식별자',
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    image_url VARCHAR(500) NOT NULL COMMENT '이미지 URL',
    sort_order INT DEFAULT NULL COMMENT '정렬 순서',
    caption VARCHAR(255) DEFAULT NULL COMMENT '캡션',
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_images_post_id (post_id),
    INDEX idx_post_images_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 첨부 이미지 테이블';

-- ============================================
-- 8. 게시글 좋아요 (Post_Likes)
-- ============================================
CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시글 좋아요 고유 식별자',
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    username VARCHAR(100) NOT NULL COMMENT '사용자 아이디',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    CONSTRAINT uq_post_likes UNIQUE (post_id, username),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_likes_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 좋아요 테이블';

-- ============================================
-- 9. 스토리 좋아요 (Story_Likes)
-- ============================================
CREATE TABLE IF NOT EXISTS story_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '스토리 좋아요 고유 식별자',
    story_id BIGINT NOT NULL COMMENT '스토리 ID',
    username VARCHAR(100) NOT NULL COMMENT '사용자 아이디',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    CONSTRAINT uq_story_likes UNIQUE (story_id, username),
    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    INDEX idx_story_likes_story_id (story_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스토리 좋아요 테이블';

-- ============================================
-- 초기 데이터
-- ============================================
-- 관리자 계정(admin/1111)은 DataInitializer.java에서 자동 생성됩니다.

-- ============================================
-- 인덱스 및 제약조건 확인
-- ============================================
-- SHOW CREATE TABLE members;
-- SHOW CREATE TABLE posts;
-- SHOW CREATE TABLE comments;
