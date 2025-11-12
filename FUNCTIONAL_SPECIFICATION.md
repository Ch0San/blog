# 기능 명세 (구현 기준)

본 문서는 PROJECT_SPECIFICATION.md와 실제 코드 구현(컨트롤러, 서비스, 시큐리티 설정)을 기준으로 시스템의 기능을 요약합니다. 각 항목은 “무엇을/어떻게 동작하는지” 중심으로 정리했습니다.

## 1. 사용자 인증/인가
- 로그인: 사용자는 이메일(=username)과 비밀번호로 로그인한다. Spring Security 폼 로그인(`/member/signin`)과 세션 기반 인증을 사용한다.
- 로그인 성공: 기본 성공 URL은 `/`로 리다이렉트된다. Remember‑Me 사용 시 14일 유효 쿠키가 발급된다.
- 로그인 실패: `/member/signin?error`로 리다이렉트한다.
- 로그아웃: `/member/signout` 호출 시 세션 무효화 및 Remember‑Me 쿠키 삭제 후 `/?logout`으로 이동한다.
- 권한/인가: `ROLE_USER`, `ROLE_ADMIN` 두 권한을 사용한다. 글/스토리/공지 작성·수정·삭제 및 일부 관리 기능은 `ADMIN`만 허용한다. 조회(목록/상세)와 정적 리소스는 익명 허용 범위를 명시적으로 열어둔다.
- 비밀번호 저장: 현재 개발 편의를 위해 NoOpPasswordEncoder(평문)가 설정돼 있다. 운영에서는 반드시 해시(BCrypt 등)로 교체해야 한다.

## 2. 회원 관리
- 회원가입: `POST /member/signup`에서 username(이메일), password, nickname, email, phoneNumber, address를 받아 저장한다.
- 회원정보 조회/수정: 인증 사용자만 `/member/update` 접근 가능. 현재 비밀번호 검증 후 이메일/연락처/주소 변경, 비밀번호는 입력 시에만 변경한다.
- 회원탈퇴: `POST /member/delete` 호출 시 계정 삭제, 세션 무효화, Remember‑Me 쿠키 삭제 후 `/?accountDeleted`로 이동한다.
- 관리자 기능: 태그 관리(`/member/tag-update`), 소개글 관리(`/member/introduction-update`)는 `ADMIN`만 접근 가능하다.

## 3. 게시글(Posts)
- 목록: `GET /posts` 페이징(페이지당 10개), 선택적 카테고리 필터를 지원한다. 리스트 노출용으로 본문 HTML 태그를 제거해서 요약 텍스트를 만든다.
- 검색: `GET /posts/search?field=title|content|tags&q=...` 형태로 검색한다.
- 상세: `GET /posts/{id}`는 조회수를 1 증가시킨다. 본문에 포함되지 않은 첨부 이미지/파일은 별도 목록으로 모델에 제공한다.
- 작성: `GET /posts/write` 폼, `POST /posts/write`로 저장한다(ADMIN 전용). 썸네일은 업로드 파일 우선, 없으면 hidden으로 전달된 기존 URL을 사용한다. 공개 여부 `isPublic` 플래그를 지원한다.
- 수정/삭제: `GET /posts/edit/{id}`, `POST /posts/edit/{id}` 및 `POST /posts/delete/{id}`(ADMIN 전용) 경로로 처리한다.
- 파일/이미지: 본문 내 사용 이미지(`/uploads/images/...`)를 추출해 임시 업로드 중 미사용 이미지를 정리한다(세션 키 `tempUploadedImages`). 일반 파일, 동영상 URL, 썸네일 URL을 함께 저장/노출한다.
- 다운로드: 첨부 파일 다운로드 엔드포인트(`/posts/download`)를 제공한다.

### 게시글 세부 흐름
- 목록(익명 허용)
  - 진입: `GET /posts` (옵션: `category`, `page`)
  - 처리: 서비스 페이징 조회(10개), 본문 HTML 태그 제거해 요약 표시, 카테고리 탭 활성화
  - 결과: `posts/list.html` 렌더링, 페이지네이션 노출
- 검색(익명 허용)
  - 진입: `GET /posts/search?field=title|content|tags&q=&page=`
  - 처리: 필드별 검색, 요약 처리 동일, 검색 파라미터 유지한 페이지네이션 링크 구성
  - 결과: `posts/list.html` 렌더링
- 상세(익명 허용)
  - 진입: `GET /posts/{id}`
  - 처리: 조회수 +1, 본문에 포함되지 않은 첨부 목록 분리, 최신 글(5) 조회, 로그인 사용자의 게시글/댓글 좋아요 상태 조회
  - 결과: `posts/detail.html` 렌더링, 좋아요 버튼(인증 시), 공유 버튼, 태그 표시, 첨부/다운로드 섹션, 최신글 카드 그리드
- 좋아요(인증 필요, AJAX)
  - 진입: `POST /posts/{postId}/like`
  - 처리: 토글 후 `isLiked`, `likeCount` 반환(401 처리 포함)
  - 결과: `posts/detail.js`에서 카운트 및 버튼 상태 갱신
  - 오류 처리 UX: 401 응답 시 알림 후 `/member/signin`으로 이동, 네트워크/기타 오류 시 알림(`좋아요 처리에 실패했습니다`).
- 댓글(인증 필요)
  - 생성: `POST /posts/{postId}/comments` → 상세로 리다이렉트
  - 수정: `POST /comments/{commentId}/update` (작성자 검증 실패 시 `?error=` 쿼리 부여)
  - 삭제: `POST /comments/{commentId}/delete`
  - 댓글 좋아요(인증 필요, AJAX): `POST /comments/{commentId}/like` → 성공 시 카운트/텍스트 토글, 401 시 알림 후 로그인 페이지 이동.
  - 메시지 표시: 상세 화면 상단에 `param.success`/`param.error` 존재 시 알림 배너 표시.
- 작성(ADMIN)
  - 진입: `GET /posts/write`
  - 입력: 제목, 작성자, 카테고리, 태그, 본문(라이브 프리뷰 에디터), 섬네일(파일 또는 본문 이미지 선택), 일반 파일 첨부, 동영상 URL(옵션), 공개 여부
  - 본문 이미지: `inlineImageUpload`로 `/api/uploads/image`에 AJAX 업로드(최대 4MB), 업로드 URL은 hidden `imageUrls`로 수집
  - 섬네일 선택: 본문 이미지에 마우스오버 시 “썸네일” 버튼으로 `thumbnailUrl` 세팅, 파일 입력 선택 시 미리보기 교체
  - 제출: `POST /posts/write` 멀티파트, 서버가 임시 업로드 중 미사용 이미지를 정리
- 수정/삭제(ADMIN)
  - 수정: `GET /posts/edit/{id}` → `POST /posts/edit/{id}`
    - 기능: 본문 에디터, 본문 이미지 재정렬/정렬(좌/우/가운데), 구분선, 텍스트 서식(B/I/U/색상/폰트/정렬), 비디오/지도 삽입, 기존 첨부 체크박스 삭제, 썸네일 교체/삭제
  - 삭제: `POST /posts/delete/{id}` (확인 다이얼로그)
- 업로드 정리
  - 클라이언트: 페이지 이탈/취소 시 `/api/uploads/cleanup`로 사용 이미지 목록 전달해 임시 이미지 삭제
  - 서버: 저장 시 본문에서 사용된 `/uploads/images/...`만 유지하고 나머지 임시 파일 삭제

### 게시글 화면 요건
- 목록 화면(`templates/posts/list.html`)
  - 카테고리 탭: 전체/일상/기술/여행/맛집 등, 활성 탭 강조
  - 리스트 항목: 제목(링크), 작성자/일시, 요약(HTML 태그 제거, 공백 치환, 최대 150자), 썸네일 또는 카테고리 배지
  - 페이지네이션: 이전/번호/다음, 검색/카테고리 파라미터 유지
  - ADMIN만 “글쓰기” 버튼 노출
- 상세 화면(`templates/posts/detail.html`)
  - 헤더: 카테고리, 제목, 작성자, 일시, 조회수, 좋아요 수/버튼(인증 시), 공유 버튼(링크복사/트위터/페이스북)
  - 본문: HTML 그대로 렌더링(지도/비디오/이미지 포함)
  - 첨부: `filesUrl` 기반 다운로드 리스트
  - 최신 글: 썸네일 카드 그리드(4~5개)
  - 댓글: 목록/작성/수정/삭제, 댓글 좋아요 상태 반영(IDs 세트)
- 작성/수정 화면(`templates/posts/write.html`, `edit.html` + `static/js/posts/write.js`, `edit.js`)
  - 에디터: contenteditable 프리뷰 + 숨김 textarea 싱크, 서식/정렬/구분선/이미지/비디오/지도 툴바, 이미지 정렬(좌/우/중앙 래핑), 썸네일 지정 버튼(이미지 호버)
  - 파일: 일반 첨부 프리뷰(파일명/용량), 이미지 인라인 업로드(4MB), 썸네일 파일(10MB)
  - Kakao 지도: SDK 자동 주입(meta 키 필요), 지도 블록 삽입/미리보기
  - 사이드바: 썸네일 미리보기/삭제 체크, 일반 파일 첨부 목록 프리뷰
  - 유효성: 제목/작성자/본문 필수, 파일/이미지 용량 제한, CSRF 메타 태그/헤더 포함

#### 게시글 에디터 툴바 상세 명세
- 공통 동작
  - 프리뷰 영역(`#livePreview`)는 contenteditable이며, 모든 편집 결과는 숨김 textarea(`#content`)에 실시간 반영한다.
  - CSRF 메타(`_csrf`, `_csrf_header`)를 읽어 AJAX 업로드에 헤더로 포함한다.
  - 서식 프리셋: 글꼴/크기/색상 선택 시 이후 타이핑 문자에만 적용되며, 선택 영역이 있을 경우 해당 부분만 스타일을 적용한다.
- 텍스트 서식
  - 굵게(B), 이탤릭(I), 밑줄(U): `document.execCommand`로 토글, 툴바 버튼의 active 상태를 반영한다.
  - 글자색: 컬러 피커(`tb-color`) 선택값을 현재 타이핑 또는 선택 영역에 적용한다.
  - 폰트/크기: `tb-fontFamily`, `tb-fontSize` 선택값을 프리셋으로 저장, 이후 입력 또는 선택 영역에 적용한다.
  - 정렬: 좌/가운데/우 정렬 버튼이 블록 또는 선택 컨텍스트에 적용된다.
- 구분선
  - `tb-divider` 클릭 시 `<hr>`를 커서 위치에 삽입한다.
- 이미지 삽입/정렬/썸네일 지정
  - 인라인 이미지 업로드: `inlineImageUpload` 파일 선택 → `/api/uploads/image`(최대 4MB)로 업로드 → 응답 URL을 `<img src...>`로 커서 위치에 삽입.
  - 업로드 추적: 응답 URL을 hidden `imageUrls`에 콤마로 수집, 임시 업로드 정리에 사용한다.
  - 정렬: 이미지 래퍼에 `image-float-left/right/centered` 클래스를 부여하여 좌/우 플로팅 또는 중앙 배치, 텍스트 워랩 처리.
  - 썸네일 버튼: 프리뷰 이미지에 마우스오버하면 “썸네일” 버튼 표시 → 클릭 시 hidden `thumbnailUrl`을 해당 이미지 URL로 설정하고 썸네일 미리보기를 갱신(파일 입력은 초기화).
- 비디오 삽입
  - `tb-video` 클릭 시 URL 입력 프롬프트 → YouTube URL이면 `iframe` 임베드, 그 외 MP4 링크는 `<video controls>` 블록으로 삽입.
- 지도 삽입(Kakao Maps)
  - SDK 주입: meta `kakao-js-key`를 이용해 `https://dapi.kakao.com/v2/maps/sdk.js?autoload=false&libraries=services`를 동적 로드 후 `kakao.maps.load`로 초기화.
  - 지도 블록 삽입: `tb-map` 클릭 → 위치 선택 후 `insertKakaoMapBlock(lat,lng,label)` 호출 → 고유 ID의 지도 컨테이너와 초기화 스크립트를 함께 삽입, 즉시 미리보기 렌더링.

#### 키보드/포커스 및 커서 제어
- 커서 보정: 선택이 프리뷰 외부일 경우 프리뷰 끝으로 커서를 강제 이동한 후 삽입을 수행한다.
- 선택 적용: 선택 영역이 있을 때는 해당 영역에 스타일/span 래핑을 적용하고, 없을 때는 프리셋을 이용해 타이핑 문자를 스타일링한 개별 span으로 입력한다.
- 삽입 후 케어릿 위치: HTML 삽입 시 마지막 노드 뒤로 커서를 이동시켜 연속 입력이 자연스럽게 이어지도록 한다.

#### 카카오 지도 삽입 모달 흐름(UX)
- 진입: 에디터 툴바의 “지도” 버튼 클릭.
- SDK 준비: 키 검증 및 스크립트 로드(최대 15초 대기, 실패 시 경고).
- 위치 선택: 지도 모달에서 위치를 선택(검색/핀 이동)하고 확인.
- 삽입: 본문에 지도 블록이 삽입되고 즉시 미리보기에서 렌더링된다(마커 포함). 라벨 입력 시 지도 하단 캡션을 함께 출력한다.
- 실패 처리: SDK 로드/초기화 실패, DOM 삽입 실패 등은 알림창으로 사용자에게 안내한다.

#### 미디어 크기 조절(이미지/비디오/지도) UX
- 공통: 미디어 요소에 `resizable-media` 클래스를 부여, 드래그 제스처로 크기 조절. 조절 중 화면 좌상단 근처에 `가로×세로(px)` 오버레이 인디케이터를 표시한다.
- 이미지: 이미지 위를 드래그하면 가로 폭을 변경한다. 최소 100px, 최대 에디터 폭(preview 영역)을 넘지 않도록 제한하며, `max-width`를 해제해 고정 폭을 적용한다.
- 비디오: `resizable-video` 블록 드래그 시 가로 폭을 변경하고, 세로는 16:9 비율로 자동 환산한다. 최소 200px, 최대 에디터 폭으로 제한한다.
- 지도: `resizable-map` 드래그 시 가로/세로를 함께 변경한다. 가로는 에디터 폭 이하, 세로는 최소 200px 이상으로 제한한다. 변경 즉시 미리보기 크기에 반영된다.
- 종료: 마우스 업 시 크기 조절 모드가 해제되고 인디케이터를 숨긴다.

#### 임시 업로드 자동 정리 트리거
- 수집: 본문 인라인 업로드된 이미지 URL을 hidden `imageUrls`와 `uploadedImages`(Set)로 추적.
- 저장: 폼 제출 시(`submit`) textarea를 최신 프리뷰 HTML로 싱크하고, 서버 저장 후 정리를 중복 호출하지 않도록 `cleanupSent=true`로 설정한다.
- 이탈: 페이지 이탈(`beforeunload`) 시 업로드 이력이 있고 `cleanupSent=false`이면 정리 요청을 전송한다.
  - 방식: `navigator.sendBeacon('/api/uploads/cleanup', FormData)`(가능 시) 또는 `fetch(..., { keepalive:true })` 백업.
  - keepUsed: 일반 이탈은 `keepUsed=true`로 본문에서 사용된 이미지만 유지, 취소 플로우(사용자 취소 버튼 등)는 `keepUsed=false`로 모두 삭제.
- 취소: 사용자 취소 동작 시 `isCancelling=true`로 표시 후 즉시 정리 요청을 보내고 폼 제출을 막는다.

## 4. 댓글(Comments)
- 생성: `POST /posts/{postId}/comments`(인증 필요). 작성자는 현재 로그인 사용자명으로 기록된다.
- 수정: `POST /comments/{commentId}/update`(인증 필요). 작성자 본인 검증 실패 시 `?error=메시지`와 함께 상세 화면으로 리다이렉트한다.
- 삭제: `POST /comments/{commentId}/delete`(인증 필요).

## 5. 좋아요(Likes)
- 게시글 좋아요: `POST /posts/{postId}/like`(인증 필요). 토글 동작 후 JSON으로 `{"isLiked": boolean, "likeCount": number}` 반환한다.
- 인증 실패: 401과 `{"message":"UNAUTHORIZED"}`를 반환한다.
- 댓글 좋아요: 댓글에 대해서도 유사한 토글 및 사용자가 좋아요한 댓글 ID 세트를 상세 페이지 렌더링에 제공한다.

## 6. 스토리(Stories)
- 조회: 목록/상세/검색은 익명 허용이다.
- 작성/수정/삭제: `ADMIN`만 가능하다.
- 좋아요/댓글: 게시글과 유사한 패턴으로 처리하며 인증이 필요하다.

### 스토리 세부 흐름
- 목록(익명 허용)
  - 진입: `GET /stories` (옵션: `category`, `page`)
  - 처리: 카드 그리드로 썸네일/비디오 프레임/카테고리 배지/제목/요약/조회/좋아요 표시
  - 결과: `stories/list.html` 렌더링, 페이지네이션
- 검색(익명 허용)
  - 진입: `GET /stories/search?field=title|content|tags&q=&page=`
  - 처리/결과: 목록과 동일 템플릿
- 상세(익명 허용)
  - 진입: `GET /stories/{id}`
  - 처리: 비디오 URL이 있으면 플레이어 노출, 없으면 썸네일 표시, 태그 렌더링, 좋아요 상태(인증 시) 조회
  - 결과: `stories/detail.html` 렌더링, ADMIN에게만 수정/삭제 버튼 노출(삭제 확인)
- 좋아요(인증 필요, AJAX)
  - 진입: `POST /stories/{id}/like`
  - 처리: 토글 후 `isLiked`, `likeCount` 반환, `stories/detail.js`가 버튼/카운트 갱신
  - 오류 처리 UX: 401 응답 시 알림 후 `/member/signin`으로 이동, 기타 오류는 알림 표시.
- 댓글(인증 필요)
  - 생성: `POST /stories/{storyId}/comments`
  - 수정: `POST /stories/comments/{commentId}/update` (작성자 검증, 에러 시 `?error=`)
  - 삭제: `POST /stories/comments/{commentId}/delete`
  - 댓글 좋아요(인증 필요, AJAX): `POST /stories/comments/{commentId}/like` → 성공 시 카운트/텍스트 토글, 401 응답 시 알림 후 로그인 페이지 이동, 기타 오류는 알림 표시.
  - 메시지 표시: 상세 화면 상단에 `param.success`/`param.error` 존재 시 알림 배너 표시.
- 작성/수정/삭제(ADMIN)
  - 작성: `GET /stories/write` → `POST /stories/write`
    - 입력: 제목, 작성자, 카테고리, 태그, 설명(짧은 본문), 썸네일(옵션, 10MB), 동영상 파일 필수(100MB)
    - 미리보기: 썸네일 이미지, 동영상 파일 프리뷰(UI에서 파일명/용량/미디어 프리뷰)
  - 수정: `GET /stories/edit/{id}` → `POST /stories/edit/{id}`
    - 기능: 메타 필드 변경, 썸네일 교체/삭제 체크, 동영상 교체(옵션)
  - 삭제: `POST /stories/delete/{id}` (확인 다이얼로그)

### 스토리 화면 요건
- 목록 화면(`templates/stories/list.html`)
  - 카테고리 탭: 전체/여행/맛집/기술/브이로그, 활성 탭 강조
  - 카드: 썸네일 이미지 또는 비디오 미리보기(자동 재생 없음, hover 시 불투명도 조절), 카테고리 배지, 제목, 요약(60자), 날짜/조회/좋아요
  - 페이지네이션: 검색/카테고리 파라미터 유지
  - ADMIN만 “글쓰기” 버튼 노출
- 상세 화면(`templates/stories/detail.html`)
  - 헤더: 카테고리, 제목, 작성자, 일시, 조회수, 좋아요 수/버튼(인증 시)
  - 본문: 동영상 플레이어 또는 썸네일, 설명 HTML 렌더링, 태그 표시
  - 액션: 목록으로, ADMIN 수정/삭제(확인 다이얼로그)
  - 댓글: 목록/작성/수정/삭제, 댓글 좋아요 토글(AJAX)
- 작성/수정 화면(`templates/stories/write.html`, `edit.html` + `static/js/stories/write.js`, `edit.js`)
  - 입력: 제목/작성자/카테고리/태그/설명
  - 미디어: 썸네일(10MB), 동영상(100MB), 파일 선택 시 미리보기/파일명/용량 표시
  - 유효성: 제목/작성자/동영상(작성 시) 필수, CSRF 메타 태그/헤더 포함

## 7. 공지사항(Notices)
- 조회: 목록/상세는 익명 허용이다.
- 작성/수정/삭제: `ADMIN`만 가능하다.

## 8. 파일 업로드 API
- 이미지 업로드: `POST /api/uploads/image`(multipart/form‑data, 키 `file`). 최대 4MB. 성공 시 `{"url":"/uploads/images/…"}` 반환.
- 임시 파일 정리: `POST /api/uploads/cleanup`에 `usedImages`(콤마 구분 URL 목록) 전달 시 세션에 기록된 임시 업로드 중 미사용 이미지를 삭제한다. 결과로 `{"deleted": n}` 또는 상태 메시지를 반환한다.
- 업로드 경로: 서버 로컬 `uploads/images` 하위에 일자‑UUID 파일명으로 저장하고, `/uploads/images/...`로 접근한다.

## 9. 홈/방문자 집계
- 방문자 카운트: 하루 1회만 증가하도록 `visited_today` 쿠키를 사용한다. 첫 방문 시 오늘 날짜 값을 쿠키로 설정하고 만료를 자정까지로 지정한다.
- 대시보드 데이터: 인기글/최신글, 최근 공지, 오늘/누적 방문자 수, 태그 목록, 소개글, 히어로 이미지 URL을 모델에 담아 `index` 템플릿을 렌더링한다.

## 10. 사이트 설정
- 키‑값 설정(site_settings): 태그(`site_tags`), 소개글(`site_introduction`), 히어로 이미지(`site_hero_image_url`) 등을 저장/조회한다. 관리자 화면에서 갱신한다.

## 11. 프론트엔드/템플릿
- 템플릿: Thymeleaf 기반. 보안 태그(Sec)로 인증/권한별 UI 제어를 적용한다.
- 정적 리소스: `/css/**`, `/js/**`, `/images/**`, `/uploads/**` 경로를 익명 허용한다.
- Kakao 지도: 게시글 작성/수정/상세 등 필요한 화면에서 Kakao Maps JS SDK를 사용하며, 키는 `application.properties`에서 주입된다.

## 12. 유효성/에러 처리(요약)
- 폼 유효성: 서버단에서 필수값 존재 여부를 확인한다(간단 검증). 댓글 수정 시 권한 위반 등은 쿼리 파라미터로 에러 코드를 전달한다.
- 업로드 오류: 이미지 용량 초과, 빈 파일 등은 `{"error":"..."}` 형태로 반환한다.
- 인증 오류: AJAX 요청 시 401을 명시적으로 반환한다.

## 13. 보안/운영 고려사항
- 비밀번호 해시: 운영 전환 시 BCrypt 등으로 교체 필요.
- 권한 범위: 관리자 전용 라우트는 모두 `hasRole('ADMIN')`로 보호한다.
- 공개 범위: GET 기반 조회/정적 리소스/회원가입/로그인/비밀번호 찾기 등은 명시적으로 허용한다.
