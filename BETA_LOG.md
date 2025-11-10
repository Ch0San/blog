# v0.0.0.3 - 2025-11-10

보강 및 버그 수정
- 미리보기 스크립트 노출 제거: 컨텐츠 요약 생성 시 <script>/<style> 블록까지 제거하도록 서버 로직 보강
  - 수정 파일: src/main/java/com/example/blog/controller/HomeController.java, PostController.java (stripHtmlTags)
- 이미지 리사이즈 반영: 상세 페이지에서 이미지/비디오/지도가 항상 가로로 꽉 차던 문제 수정
  - 수정 파일: src/main/resources/static/css/style.css
  - 변경점: .post-content img.resizable-media는 max-width만 제한(가로 100% 강제 제거),
           .resizable-video/.kmap도 max-width만 적용, .resizable-map는 min-height만 유지
- 수정 폼 썸네일 업로드 불가 버그 수정: input name 누락으로 서버 매핑 실패 → name="thumbnailFile" 추가
  - 수정 파일: src/main/resources/templates/posts/edit.html

검증 메모
- 목록/메인 미리보기에서 지도 삽입 시 스크립트/URL 텍스트가 노출되지 않는지 확인
- 에디터에서 이미지 폭(px)을 조절해 저장 후 상세에서 동일하게 표시되는지 확인
- 수정 화면에서 새 썸네일 파일 선택 후 저장 시 교체 반영되는지 확인


# v0.0.0.2 - 2025-11-10

개요
- JS 파일 전반의 한글 깨짐(mojibake) 제거 및 중복/오염 코드 정리
- Kakao 지도 연동 구조는 유지하되, 주입/초기화 흐름을 명확히 하고 안내 문구를 한국어로 통일
- 잘못된 CSS 문자열(예: `border: 1px x …`)을 `border: 1px solid …`로 교정

변경 사항
- stories/detail.js: 좋아요/댓글 좋아요 로직 정리, 상단 깨진/중복 블록 제거, 한국어 주석/문구 통일, CSRF 헤더 기본값 유지
- stories/list.js: 썸네일/비디오 hover 미리보기 스크립트 전면 정리(1초 지연 후 재생, 최대 5초 재생 후 복구, 자동재생 차단 시 정지 프레임 복원), 중복 제거
- stories/edit.js: 동영상/썸네일 미리보기 및 용량 검증(100MB/10MB) 문구 정리, ‘썸네일 삭제 예정’ 오버레이 토글 안정화
- stories/write.js: 작성 화면 미리보기/용량 검증 한국어화, 안전 라벨([VIDEO]/[IMAGE]) 적용
- posts/edit.js: 파일 전체를 정상 한글로 재작성(미리보기/툴바/썸네일 버튼/인라인 업로드+CSRF/지도 모달(수정)/sendBeacon 정리/미디어 리사이즈 유지)
- posts/write.js: 파일 전체를 정상 한글로 재작성(미리보기/툴바/썸네일 버튼/인라인 업로드/지도 모달(작성)/정리 로직/리사이즈 유지). CSS 오타 교정, 파일 목록 미리보기/용량 경고 한국어화, 리사이즈 인디케이터 `px × px` 표기

브라우저 경고 관련
- Edge/Chrome 콘솔의 Kakao SDK 경고(document.write, parser‑blocking, cross‑site)는 SDK 내부 구현에 따른 일반 경고로, 로딩 실패가 없다면 기능 영향은 없음
- 최소화 방안: 버튼 클릭 시 지연 로딩 또는 `<script defer>` 사용으로 파서 차단 빈도 완화 가능

검증 체크리스트
- 스토리 목록 hover 미리보기, 상세 좋아요/댓글 좋아요 토글 동작 확인
- 글 작성/수정: 툴바(굵게/기울임/밑줄/정렬/색상/폰트), 썸네일 버튼, 인라인 이미지 업로드(4MB 제한), 첨부 목록 프리뷰(10MB 제한) 확인
- 지도 모달(작성/수정): 검색/지도 클릭/삽입 정상, 삽입된 블록 즉시 렌더
- 취소/페이지 이탈 시 임시 업로드 정리(sendBeacon) 호출 확인


# v0.0.0.1 - 2025-10-30

개요
- stories/list.js 중심으로 목록 hover 미리보기 UX를 개편하고, 템플릿 연계 코드를 단순화

변경 사항(요약)
- stories/list.js: 이미지/비디오 카드에서 썸네일 우선 표시 → hover 시 비디오 미리보기 전환, 최대 5초 재생 후 복원, 충돌 방지 로직 추가
- templates/stories/list.html: 경로 가드 보강 및 표시 마크업 정리
- 불필요/중복 코드 제거와 구조 정리로 가독성/유지보수성 개선
