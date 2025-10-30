# v1.0.0 - 2025-10-29
요약 : 포스트/스토리 작성·수정 화면의 UI/UX를 전면 개선하여, 사이드바·툴바·파일/썸네일 업로드 기능을 완전히 통일하고, 	레이아웃과 스타일을 일관성 있게 정비했습니다.
모든 JS 코드를 외부 파일로 모듈화하고, 주요 오류를 수정했으며, 전체 코드 스타일을 정리했습니다.
마지막으로 깃 히스토리를 리셋하여 v1.0.0으로 새롭게 시작했습니다.

### 변경사항(한글)
- 포스트/스토리 작성·수정 화면의 UI/UX 대대적 개선
- 사이드바(aside), 툴바, 파일/썸네일 업로드 UI/로직 완전 통일
- flex 2단 레이아웃, sticky 사이드바, 툴바/업로드/미리보기 일관화
- 썸네일 삭제 체크박스 위치 및 스타일 개선(한 줄, 레이블 우측)
- 헤더 가림 방지용 sticky top 패딩(100px) 적용
- 모든 JS 코드 외부 파일로 분리 및 모듈화
- posts/stories의 write/edit.html 내 인라인 스크립트 → static/js/posts|stories/write.js, edit.js로 이동
- defer 속성으로 안전하게 로드, DOMContentLoaded 내 robust 이벤트 바인딩
- 주요 JS 오류 수정
- ReferenceError: thumbnailFileInput is not defined 등 DOM 미존재 오류 방지
- 이벤트 바인딩 시점 및 대상 보강
- 전체 코드/템플릿 들여쓰기 및 스타일 정리
- 깃 히스토리 리셋: 기존 모든 커밋 삭제, v1.0.0으로 오리진 force push 및 CHANGELOG.md 갱신
- 코드 리팩토링 작업 완료

### 영향범위
- templates: posts/write.html, posts/edit.html, stories/write.html, stories/edit.html (UI/마크업/스크립트 구조 통일)
- static/js: posts/write.js, posts/edit.js, stories/write.js, stories/edit.js (모듈화 및 오류 수정)
- static/css: style.css (sticky, flex, 썸네일 등 스타일 개선)
- CHANGELOG.md: v1.0.0 릴리스 노트 추가
- git: 모든 히스토리 삭제, v1.0.0 force push

---

# v0.4.1 — 2025-10-29

요약: 포스트/수정 에디터 툴바 버튼(볼드, 이탤릭, 밑줄 등) 클릭이 동작하지 않던 문제를 해결하고, 불필요한 썸네일 업로드 관련 JS 오류를 제거했습니다.

### 변경사항 (한글)
- 에디터 툴바 버튼(B, I, U) 클릭 이벤트가 동작하지 않던 현상 수정
	- 버튼 내부에 불필요한 태그 제거 및 구조 일원화
	- CSS/JS 구조 점검 및 이벤트 정상화
- write.js에서 "thumbnailFileInput is not defined" ReferenceError 발생 코드 제거
	- 더 이상 사용하지 않는 썸네일 업로드 관련 코드 완전 삭제
- 기타: 전체 코드/템플릿/JS 동기화 및 불필요 코드 정리

### 영향 범위
- templates: `posts/write.html`, `posts/edit.html` (툴바 버튼 구조 일원화)
- static/js: `posts/write.js` (불필요 코드 제거, 오류 수정)

---

# Changelog

## 0.4.0 — 2025-10-29

요약: 공지사항 게시판을 추가하고, 메인 화면에 공지 미리보기를 노출했습니다. 공지는 누구나 열람 가능하며 글쓰기/수정/삭제는 관리자만 가능하도록 보안을 설정했습니다. 스타일은 기존 style.css를 재사용하고, 인라인 스타일을 CSS 클래스로 분리했습니다.

### 변경사항 (한글)
- 공지사항 게시판 추가 (댓글/좋아요/미디어/태그 없음)
	- DB: `create_tables.sql`에 `notices` 테이블 추가(id, title, content, author, view_count, created_at, updated_at)
	- 도메인/저장소/서비스: `Notice` 엔티티, `NoticeRepository`, `NoticeService` 추가
	- 컨트롤러: `NoticeController` 추가
		- GET `/notice` 목록(공개), GET `/notice/{id}` 상세(공개, 조회수 증가)
		- GET `/notice/write`, POST `/notice/write` (ADMIN)
		- GET `/notice/edit/{id}`, POST `/notice/edit/{id}` (ADMIN)
		- POST `/notice/delete/{id}` (ADMIN)
	- 템플릿: `templates/notice/` 하위에 `list.html`, `detail.html`, `write.html`, `edit.html` 생성
		- 모든 POST 폼에 CSRF hidden 필드 포함
- 메인 페이지 연동
	- `HomeController`에서 최근 공지 5개 `recentNotices` 모델 추가
	- `index.html` 사이드바에 공지 카드 추가(“공지사항”/전체보기/ADMIN 작성 버튼)
	- 공지 카드 인라인 스타일 제거 → CSS 클래스로 분리(`.notice-card-header`, `.notice-actions`, `.notice-list`, `.notice-item`, `.notice-title`, `.notice-date` 등)
- 보안 설정
	- `SecurityConfig`에 공지 경로 권한 추가: GET `/notice/**`는 공개, `/notice/write|edit/**|delete/**`는 ADMIN 전용
- 스타일
	- `style.css`에 공지 카드용 클래스 추가(사이드바 위젯 스타일 범위 내)
- 테스트 데이터
	- `test_data.sql`에 `TRUNCATE TABLE notices;` 추가
	- 점검용 공지 13건 시드 데이터 추가

### 영향 범위
- config: `SecurityConfig`
- controller: `NoticeController`, `HomeController` (recentNotices 주입)
- domain/repository/service: `Notice`, `NoticeRepository`, `NoticeService`
- templates: `notice/list.html`, `notice/detail.html`, `notice/write.html`, `notice/edit.html`, `index.html`
- static/css: `style.css` (공지 위젯 스타일 추가)
- SQL: `create_tables.sql`(테이블 추가), `test_data.sql`(시드 추가)

---

## 0.3.5 — 2025-10-29

요약: 스토리 및 포스트 썸네일 이미지 관리 UI를 개선하고, 썸네일 표시 로직을 수정하여 이미지가 정상적으로 표시되도록 안정화했습니다.

### 변경사항 (한글)
- 썸네일 삭제 UI 개선
	- 스토리/포스트 수정 페이지에서 "현재 썸네일 삭제" 체크박스를 "썸네일 이미지 변경" 레이블 옆으로 이동
	- flexbox를 사용하여 한 줄에 양쪽 정렬로 배치
	- 사용성 및 가독성 향상
- 썸네일 표시 로직 수정
	- stories/list.html: `background-image` 대신 `<img>` 태그 사용으로 변경
	- 빈 div의 높이 문제 해결 (background-image는 내용이 없으면 높이 0)
	- `object-fit:cover`로 이미지 크롭 및 비율 유지
	- 썸네일 우선순위 로직 유지 (썸네일 이미지 > 동영상 첫 프레임 > 그라데이션)
- CSS 스타일 수정
	- `.stories-thumbnail` 클래스에서 기본 그라데이션 배경 제거
	- `overflow:hidden` 속성 추가로 자식 요소 영역 제한
	- 그라데이션은 HTML에서 조건부로만 표시되도록 변경
- 썸네일 이미지 로딩 안정화
	- 이미지 경로 정상 로딩 확인 (200 OK)
	- position:absolute와 img 태그 조합으로 확실한 렌더링 보장
	- alt 속성 추가로 접근성 향상

### 영향 범위
- templates: `stories/edit.html`, `posts/edit.html` (UI 레이아웃 변경)
- templates: `stories/list.html` (썸네일 표시 로직 변경)
- static/css: `style.css` (.stories-thumbnail 스타일 수정)

---

## 0.3.2 — 2025-10-29

요약: 스토리에 커스텀 썸네일 이미지 업로드 기능을 추가하여, 동영상 첫 화면 대신 원하는 이미지를 썸네일로 설정할 수 있도록 개선했습니다.

### 변경사항 (한글)
- 스토리 썸네일 이미지 업로드 기능 추가
	- 작성/수정 시 썸네일 이미지 파일 업로드 가능 (선택사항)
	- 이미지 파일만 업로드 가능 (accept="image/*"), 최대 10MB
	- 업로드 시 실시간 미리보기 표시
- 썸네일 표시 우선순위 로직
	1. 썸네일 이미지가 있으면 썸네일 이미지 표시
	2. 썸네일이 없고 동영상이 있으면 동영상 첫 화면 표시 (기존 방식)
	3. 둘 다 없으면 그라데이션 배경 표시
- 파일 관리
	- 썸네일 저장 위치: `uploads/images/story_thumb_*.jpg`
	- 스토리 수정 시 새 썸네일 업로드하면 기존 썸네일 자동 삭제
	- 스토리 삭제 시 동영상과 썸네일 파일 모두 삭제
# v1.0.0 — 2025-10-29
- 백엔드 지원
	- StoriesController: `saveThumbnailFile()`, `deleteImageFileByUrl()` 메서드 추가
	- write/edit 엔드포인트에 `thumbnailFile` 파라미터 추가

- controller: `StoriesController` (썸네일 업로드/삭제 로직)
- templates: `stories/write.html`, `stories/edit.html` (썸네일 업로드 필드), `stories/list.html` (썸네일 표시 로직)
- domain: `Story` (thumbnailUrl 필드 활용)
---


요약: 아이디/비밀번호 찾기 기능의 URL 및 파일명을 표준화하고, Spring Security 설정을 보완하여 접근 권한 문제를 해결했습니다.
- URL 및 파일명 표준화
	- 아이디 찾기 URL: `/member/find-username` → `/member/find-id`
- 컨트롤러 메서드명 정리
	- `findUsername()` → `findId()`
- 모든 관련 템플릿 링크 업데이트
	- `signIn.html`, `findId.html`, `findPassword.html`의 링크를 새 URL로 통일

### 영향 범위
- config: `SecurityConfig` (권한 설정 추가)
- controller: `MemberController` (URL 및 메서드명 변경)
- templates: `member/findId.html` (파일명 변경), `member/signIn.html`, `member/findPassword.html` (링크 업데이트)

---

## 0.3.0 — 2025-10-29

요약: 아이디/비밀번호 찾기 기능을 추가하여 사용자 계정 복구 프로세스를 완성했습니다.

### 변경사항 (한글)
- 아이디 찾기 기능 추가
	- 이메일과 연락처로 본인 확인 후 아이디 전체 표시
	- 찾은 아이디로 바로 로그인 또는 비밀번호 찾기로 이동 가능
	- 새 페이지: `templates/member/findId.html`
- 비밀번호 찾기/재설정 기능 추가
	- 아이디, 이메일, 연락처로 본인 확인 2단계 인증
	- 본인 확인 완료 시 새 비밀번호 입력 폼 표시
	- 비밀번호 확인 입력으로 오타 방지
	- 새 페이지: `templates/member/findPassword.html`
- 로그인 페이지 개선
	- 하단에 "아이디 찾기 | 비밀번호 찾기" 링크 추가
	- 비밀번호 재설정 완료 시 성공 메시지 표시
- 백엔드 지원
	- MemberRepository: `findByEmailAndPhoneNumber`, `findByUsernameAndEmailAndPhoneNumber` 메서드 추가
	- MemberService: 아이디/비밀번호 찾기 및 재설정 메서드 추가
	- MemberController: `/member/find-id`, `/member/find-password`, `/member/reset-password` 엔드포인트 추가

### 영향 범위
- templates: `member/findId.html`, `member/findPassword.html`, `member/signIn.html`
- repository: `MemberRepository` (조회 메서드 추가)
- service: `MemberService` (계정 복구 로직)
- controller: `MemberController` (찾기/재설정 엔드포인트)

---

## 0.2.5.2 — 2025-10-28

요약: 포스트 에디터 툴바에서 이미지/동영상/지도 버튼을 2번째 줄로 이동하여 레이아웃 가독성과 사용성을 개선했습니다.

### 변경사항 (한글)
- 툴바 2열 구성
	- 1열: 글자 스타일/정렬 등 텍스트 서식 관련 컨트롤 유지
	- 2열: 미디어 삽입(🖼 이미지, 🎬 동영상, 🗺 지도) 버튼을 별도 줄로 이동
	- 버튼 간 간격과 구분이 명확해져 오작동 가능성 감소

### 영향 범위
- templates: `templates/posts/write.html`, `templates/posts/edit.html`

---

## 0.2.5.1 — 2025-10-28

요약: 리사이징 크기 인디케이터를 마우스 커서를 따라 이동하도록 변경하여 즉시성을 높였습니다.

### 변경사항 (한글)
- 인디케이터 위치 정책 변경
	- 표시 위치를 `position: fixed` 기반으로 커서 위치 근처에 노출(가독성 향상을 위해 약간의 오프셋 적용)
	- 드래그 중에만 노출되고 `mouseup` 시 자동 숨김
- UI 일관성
	- write/edit 화면 모두 동일 동작으로 통일

### 영향 범위
- static/js: `static/js/posts/write.js`, `static/js/posts/edit.js`

---

## 0.2.5 — 2025-10-28

요약: 포스트 에디터의 이미지 리사이징 및 썸네일 지정 UI를 개선했습니다. 크기 조절 시 실시간 수치 표시와 명시적인 썸네일 버튼으로 사용자 경험을 향상시켰습니다.

### 변경사항 (한글)
- 에디터 이미지 리사이징 개선
  - 이미지/동영상/지도 크기 조절 중 우측 하단에 실시간 크기 표시 (`너비px × 높이px`)
  - 검은 반투명 배경의 인디케이터로 가독성 확보, 드래그 중에만 표시
  - monospace 폰트로 수치 정렬 최적화
- 썸네일 지정 UI 개선
  - 기존: 이미지 클릭 시 즉시 썸네일 등록 (실수 가능성)
  - 변경: 이미지 마우스 오버 시 좌측 상단에 '썸네일' 버튼 표시
  - 버튼 클릭으로 명시적 지정, 실수 방지 및 직관성 향상
  - 마우스 아웃 시 버튼 자동 제거

### 영향 범위
- static/js: `write.js`, `edit.js` (리사이징 인디케이터, 썸네일 버튼 로직)

---

## 0.2.4 — 2025-10-28

요약: 스토리 영상 교체/삭제 처리 보강. 수정 시 새 영상을 업로드하면 기존 파일을 안전하게 삭제하고, 스토리 삭제 시에도 연관 영상 파일을 함께 삭제합니다.

### 변경사항 (한글)
- StoriesController
  - edit: 새 `videoFile` 업로드 시 기존 `/uploads/videos/*` 파일 삭제 후 교체 (경로 정규화/디렉터리 경계 검증)
  - delete: 스토리 삭제 전에 `videoUrl`이 가리키는 `/uploads/videos/*` 파일을 안전하게 삭제
- 보안/안정성
  - 디렉터리 traversal 방지: 정규화된 경로가 `uploads/videos` 하위인지 확인
  - 삭제 실패 시에도 사용자 흐름 유지, 데이터 정합성 우선

### 영향 범위
- controller: `StoriesController` (edit, delete 로직)
- 문서: CHANGELOG

---

## 0.2.3 — 2025-10-28

요약: 에디터 및 첨부파일 처리 관련 개선을 적용했습니다. 인라인으로 본문에 삽입된 이미지는 첨부파일 영역에 노출되지 않도록 하고, 첨부파일은 실제로 업로드된 파일만 표시되도록 정리했습니다. 또한 첨부파일 확장자 인식 및 다운로드 표시를 개선했습니다.

### 변경사항 (한글)
- 에디터/업로드 동작 개선
	- 인라인 업로드(툴바 이미지 버튼) 시 반드시 본문(`#livePreview`)에 포커스가 있어야 삽입되도록 제한
	- 본문 외부에서 이미지 삽입 시 경고 메시지 출력
	- write.js / edit.js: 인라인 업로드 추적(Set), 업로드 후 본문에만 이미지 삽입, 파일 선택 미리보기는 파일명 목록으로 간소화
- 첨부파일(Attachment) 처리 정리
	- `PostController`(write/edit): `imageFiles`로 업로드한 파일만 `PostImage`(첨부파일)로 저장
	- 에디터에서 비동기로 본문에 삽입된 `imageUrls`는 `PostImage`로 추가하지 않음(첨부파일과 분리)
	- 업로드 파일은 원본 파일명을 `caption`에 저장하고, 파일 크기 제한은 최대 10MB로 설정
- 상세보기(attachments) 표시 개선
	- `PostController.detail`: 본문(content)에 포함된 이미지 URL을 제외하고, 본문에 포함되지 않은 `PostImage`만 `attachments` 모델로 전달
	- `templates/posts/detail.html`: `attachments`를 사용하여 첨부파일 섹션 렌더링, 확장자 인식(icon) 및 확장자 대문자 표기 개선

### 영향 범위
- templates: `templates/posts/detail.html`
- controllers: `PostController` (write/edit/detail 로직 변경)
- static/js: `static/js/posts/write.js`, `static/js/posts/edit.js` (인라인 업로드 포커스 검사 및 미리보기 변경)

---

## 0.2.2 — 2025-10-28

요약: 관리자 사이트 설정 페이지에서 과거에 업로드한 메인 히어로 이미지를 미리보기 형식으로 확인하고 삭제할 수 있는 기능을 추가했습니다.

### 변경사항 (한글)
- 메인 히어로 이미지 관리 기능 강화
	- 과거 업로드 이미지 목록 표시: `uploads/images/hero_*` 파일을 수정일 기준 정렬하여 그리드 형식으로 표시
	- 썸네일 미리보기: 각 이미지를 120px 높이로 미리보기, 반응형 그리드 레이아웃(최소 180px 너비)
	- 현재 사용중 배지: 활성화된 히어로 이미지에 "현재 사용중" 표시
	- 삭제 버튼: 각 썸네일 우측 하단에 삭제 버튼 배치, 클릭 시 확인 후 파일 삭제
	- 안전 삭제 로직: 현재 사용중인 이미지 삭제 시 자동으로 기본 이미지(`/images/index_image.jpg`)로 대체
	- 반영 파일
		- 템플릿: `templates/mainImageEdit.html` (과거 이미지 섹션 추가)
		- 컨트롤러: `AdminSiteController.java` (히어로 이미지 목록 조회 로직 추가, `/admin/site/hero-image/delete` 엔드포인트 추가)
- 보안 강화
	- 경로 traversal 방지: 삭제 요청 URL 검증 및 정규화된 경로 확인
	- 파일 타입 검증: hero_ 접두사 및 이미지 확장자만 허용

### 영향 범위
- templates: `mainImageEdit.html` UI 확장
- controller: `AdminSiteController` 파일 목록 조회 및 삭제 로직 추가
- 보안: 경로 검증 및 현재 이미지 보호 로직

---

## 0.2.1 — 2025-10-28

요약: 에디터 툴바에 글자 색상 선택과 문단 정렬(왼쪽/가운데/오른쪽) 기능을 추가했습니다. 스토리 내용 검색이 존재하지 않는 content 필드를 참조하던 문제를 description 기반 검색으로 수정했습니다.

### 변경사항 (한글)
- 에디터 툴바 기능 추가
	- 글자 색상 선택(Color picker) 추가: 선택 영역에 즉시 적용, 선택이 없으면 이후 입력에 적용
	- 문단 정렬 버튼(좌/중/우) 추가: 선택 영역은 블록으로 감싸 정렬, 선택이 없으면 정렬 블록 삽입
	- 반영 파일
		- templates: `templates/posts/write.html`, `templates/posts/edit.html`
		- 클라이언트 스크립트: `static/js/posts/write.js`, `static/js/posts/edit.js`
- 버그 수정
	- StoryRepository: `findByContentContainingIgnoreCaseAndIsPublicTrue` → `findByDescriptionContainingIgnoreCaseAndIsPublicTrue`
	- StoryService: `searchByContent`가 description 기반 검색을 사용하도록 수정

### 영향 범위
- templates: write/edit 툴바 UI 변경
- static/js: 글자 색상/정렬 로직 추가, 선택/무선택 시 처리 분기
- backend: `StoryRepository`, `StoryService` 검색 메서드 시그니처/호출 변경

---

## 0.1.7 — 2025-10-28

요약: 검색 기능을 제목/내용/태그로 단순화하고, 날짜 검색을 제거했습니다. 헤더 검색 UI 및 페이지네이션 링크를 이에 맞게 정리했습니다.

### 변경사항 (한글)
- 전역 검색 옵션 정리
	- 헤더 드롭다운에서 '작성일' 제거, '내용' 추가 → 이제 '제목/내용/태그'만 선택 가능
	- 날짜 입력 필드 및 토글 스크립트 제거
- 포스트 검색 로직 개편
	- PostController: 검색 파라미터를 field, q, page로 단순화 (date 관련 파라미터/모델 속성 제거)
	- PostService/PostRepository: 내용 검색(searchByContent) 추가
- 스토리 검색 로직 개편
	- StoriesController: 검색 파라미터를 field, q, page로 단순화 (date 관련 파라미터/모델 속성 제거)
	- StoryService/StoryRepository: 내용 검색(searchByContent) 추가
- 페이지네이션 링크 정리
	- posts/list.html, stories/list.html에서 검색 페이지네이션 URL의 startDate/endDate 제거

### 영향 범위
- templates: fragments/header.html, posts/list.html, stories/list.html
- controllers: PostController, StoriesController
- services/repositories: PostService/Repository, StoryService/Repository

---

## 0.1.6 — 2025-10-28

요약: 에디터에서 업로드된 이미지를 임시로 추적하고, 저장 시 미사용 이미지를 자동 삭제하며, 페이지 이탈(취소/뒤로가기) 시에도 정리되도록 보강.

### 변경사항 (한글)
- 업로드 이미지 임시 추적 도입
	- UploadController: 업로드 성공 시 세션에 임시 업로드 목록(tempUploadedImages) 기록
	- `/api/uploads/cleanup` 엔드포인트 추가: 사용되지 않은 임시 업로드 파일 일괄 삭제
- 글 저장/수정 시 자동 정리
	- PostController: 저장/수정 완료 후 본문 HTML에서 실제 사용된 `/uploads/images/...` URL만 추출하여, 세션의 임시 목록과 비교해 미사용 파일 삭제
	- `extractUsedImageUrls`, `cleanupTempUploads` 유틸 추가로 재사용성 확보
- 페이지 이탈(취소) 시 정리
	- write.js, edit.js: 업로드된 이미지 URL을 Set으로 추적하고, `beforeunload` 시 본문에 남아있는 URL만 서버로 전송하여 나머지 파일 삭제(`navigator.sendBeacon` 사용)
- 사용자 경험
	- 저장 시 본문에 남겨둔 이미지들만 유지되고, 불필요한 파일은 자동 정리되어 스토리지 누수를 방지

### 영향 범위
- 서버: `UploadController`, `PostController`
- 클라이언트: `static/js/posts/write.js`, `static/js/posts/edit.js`
- 업로드 경로: `uploads/images`

---

## 0.1.4 — 2025-10-28

요약: 에디터 안정화, 동영상 플레이어 즉시 표시 및 크기 조절, 미디어 삽입 영역 제한.

### 변경사항 (한글)
- 에디터 스크립트 로딩 안정화
	- write.html, edit.html: 스크립트에 `defer` 속성 추가로 DOM 파싱 완료 후 실행 보장
	- 이벤트 바인딩 타이밍 문제 해결로 툴바 버튼/모달 동작 안정화
- 동영상 플레이어 개선
	- URL 입력 즉시 플레이어 렌더링 (YouTube iframe 또는 HTML5 video)
	- 드래그로 플레이어 크기 조절 가능 (최소 200px, 최대 본문 너비)
	- 기본 크기 640px, YouTube는 16:9 비율 자동 유지
	- `.resizable-video` 컨테이너로 통일된 리사이징 로직
- 미디어 삽입 영역 제한
	- `insertHtmlAtCursor` 함수에 영역 검증 로직 추가
	- 이미지/동영상/지도가 본문(`#livePreview`)에만 삽입되도록 제한
	- 제목/작성자/태그 등의 입력 필드에 미디어 삽입 방지
	- 본문 외부에서 미디어 버튼 클릭 시 본문 끝에 자동 삽입
- write.js, edit.js 동시 적용으로 작성/수정 페이지 일관성 유지

---

## 0.1.3 — 2025-10-28

요약: 메인 히어로 이미지 수정 플로우 추가, 포스트 좋아요 버튼 동작 개선, 관리자 보호 규칙 강화, 회원 정보 페이지 버튼 정렬 및 CSRF 표준화.

### 변경사항 (한글)
- 메인 히어로 이미지 수정
	- index: 좌측 상단 "이미지 수정" 링크로 변경, 박스 제거(텍스트만 강조)
	- mainImageEdit.html 신규: 현재 이미지 미리보기 + 업로드 폼(POST `/admin/site/hero-image`)
	- AdminSiteController: GET/POST `/admin/site/hero-image` 추가, 업로드 파일 `uploads/images` 저장 및 `site_hero_image_url` 갱신
	- HomeController: `heroImageUrl` 모델 속성 추가(기본값 `/images/index_image.jpg`)
	- SecurityConfig: `/admin/**` 관리자 보호 규칙 추가
	- style.css: `.hero-image-action`을 텍스트 링크 스타일로 정리
- 포스트 좋아요 버튼 미동작 수정
	- posts/detail.html: 스크립트 `defer` 로드로 DOM 준비 이후 실행
	- static/js/posts/detail.js: 이벤트 바인딩을 `DOMContentLoaded` 이후로 조정
- 스토리 댓글 좋아요 컨트롤러
	- 불필요 import 제거로 정리
- 회원 정보 페이지(memberUpdate)
	- 탈퇴/취소/정보 수정 버튼 한 줄 정렬
	- CSRF 파라미터명 `_csrf` 표준화
- 기타
	- .gitignore: `uploads/` 무시 추가(런타임 업로드 파일 비버전 관리)

---

## 0.0.2 — 2025-10-28

요약: 좋아요 인증 흐름 보완, 스토리 댓글 수정 기능 추가, 파일/미디어 경로 안정화, 수정 화면의 첨부 처리 개선.

### 변경사항 (한글)
- posts/detail.html: 최신글 썸네일 경로 처리 수정(Thymeleaf `@{...}` 사용)
- stories/detail.html: 동영상/썸네일 경로 수정, 댓글 수정 UI/폼 추가(게시글과 동일 패턴)
- member/memberUpdate.html: 회원탈퇴 버튼을 폼 내부로 이동, CSRF 메타 추가, JS로 안전한 탈퇴 처리
- fragments/header.html: 검색 필드에서 "작성일" 선택 시 날짜 입력 표시(초기 숨김 상태)
- PostController: 수정(edit) 시 `imageFiles` 업로드 및 `imageUrls` 반영 처리 추가, 썸네일 갱신 로직 보강
- StoriesController: 댓글 수정 엔드포인트(`/stories/comments/{id}/update`) 추가
- StoryCommentService: `updateComment`(댓글 수정) 메서드 추가(작성자 검증 포함)
- static/js/posts/detail.js: 좋아요/댓글 좋아요에 401(비로그인) 응답 처리 및 로그인 유도 추가
- SecurityConfig: 게시글 조회(GET)만 공개, 좋아요 등 POST 요청은 인증 필요하도록 경로/메서드 정렬
- PostLikeController/CommentLikeController: 비로그인 요청 시 401 JSON으로 응답하도록 방어 로직 추가

### 참고
- 기존 기능과의 호환을 유지하면서 인증·권한 흐름을 명확화했습니다.
- 경로 처리(`@{...}`) 통일로 정적/업로드 리소스 로딩 안정성을 개선했습니다.

---

## 0.0.1 — 초기 릴리스
- 블로그 기본 기능 초기 공개 (게시글/스토리, 댓글, 태그, 회원 인증 등)
