-- 초기화: 기존 데이터 전부 삭제 후 시드
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE comment_likes;
TRUNCATE TABLE story_comment_likes;
TRUNCATE TABLE comments;
TRUNCATE TABLE story_comments;
TRUNCATE TABLE post_images;
TRUNCATE TABLE post_likes;
TRUNCATE TABLE posts;
TRUNCATE TABLE stories;
TRUNCATE TABLE notices;
SET FOREIGN_KEY_CHECKS = 1;

-- 테스트용 포스트 20개 추가 (업로드 이미지 사용)
INSERT INTO posts (title, content, author, view_count, like_count, is_public, category, tags, thumbnail_url, created_at, updated_at) VALUES
('Spring Boot 3.x 주요 변경사항 정리', '<p>Spring Boot 3.0부터 많은 변경사항이 있었습니다.</p><ul><li>Java 17 LTS 최소 요구</li><li>Jakarta EE 9 전환 (javax → jakarta 패키지)</li><li>Observability 강화 (Micrometer Tracing)</li><li>Native Image 지원 개선</li></ul><p>마이그레이션 시 주의사항을 정리했습니다.</p>', 'admin', 245, 32, true, '기술', 'Spring,Java,Backend', '/uploads/images/test_icon_01.png', NOW() - INTERVAL 19 DAY, NOW() - INTERVAL 19 DAY),
('가을 단풍 여행 추천 코스 BEST 5', '<p>올 가을 꼭 가봐야 할 단풍 명소를 소개합니다.</p><h3>1. 설악산 권금성</h3><p>케이블카를 타고 올라가면 펼쳐지는 절경</p><h3>2. 내장산 단풍터널</h3><p>붉게 물든 단풍 터널을 걸으며 힐링</p><h3>3. 지리산 노고단</h3><p>운해와 단풍의 조화</p>', 'admin', 892, 67, true, '여행', '가을,단풍,여행', '/uploads/images/test_icon_02.png', NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 18 DAY),
('JPA N+1 문제 해결 방법', '<p>JPA를 사용하다 보면 자주 만나는 N+1 문제를 해결하는 방법입니다.</p><h3>1. Fetch Join</h3><pre>@Query("SELECT p FROM Post p JOIN FETCH p.comments")</pre><h3>2. EntityGraph</h3><p>@EntityGraph를 활용한 최적화</p><h3>3. Batch Size</h3><p>적절한 배치 사이즈 설정으로 쿼리 수 줄이기</p>', 'admin', 1523, 89, true, '기술', 'JPA,Hibernate,Database', '/uploads/images/test_icon_03.png', NOW() - INTERVAL 17 DAY, NOW() - INTERVAL 17 DAY),
('주말 브런치 레시피 3선', '<p>주말 아침을 특별하게 만들어줄 브런치 레시피</p><h3>아보카도 토스트</h3><p>신선한 아보카도와 수란 조합</p><h3>플러피 팬케이크</h3><p>폭신한 식감의 비결</p><h3>에그 베네딕트</h3><p>완벽한 홀란데이즈 소스 만들기</p>', 'admin', 634, 45, true, '맛집', '브런치,요리,레시피', '/uploads/images/test_icon_04.png', NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 16 DAY),
('Docker Compose로 개발 환경 구축하기', '<p>docker-compose.yml 하나로 완전한 개발 환경을 구성합니다.</p><pre>version: "3.8"\nservices:\n  db:\n    image: mysql:8.0\n  redis:\n    image: redis:alpine\n  app:\n    build: .\n    ports:\n      - "8080:8080"</pre>', 'admin', 456, 38, true, '기술', 'Docker,DevOps,개발환경', '/uploads/images/test_icon_05.png', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY),
('제주도 숨은 카페 베스트 3', '<p>제주도 현지인만 아는 카페 맛집</p><h3>애월 해안도로 카페</h3><p>오션뷰가 환상적인 곳</p><h3>한라산 숲속 카페</h3><p>자연 속 힐링 공간</p><h3>성산 한옥 카페</h3><p>전통과 현대의 조화</p>', 'admin', 721, 52, true, '여행', '제주도,카페,여행', '/uploads/images/test_icon_06.png', NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY),
('Git 브랜치 전략 비교', '<p>프로젝트 규모에 따른 적절한 브랜치 전략</p><h3>Git Flow</h3><p>대규모 프로젝트에 적합</p><ul><li>master, develop, feature, release, hotfix</li></ul><h3>GitHub Flow</h3><p>빠른 배포가 필요한 프로젝트</p><ul><li>main, feature branches</li></ul>', 'admin', 982, 71, true, '기술', 'Git,협업,버전관리', '/uploads/images/test_icon_07.png', NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY),
('초보자를 위한 등산 장비 가이드', '<p>등산을 시작하는 분들을 위한 필수 장비</p><h3>등산화</h3><p>발목 보호와 미끄럼 방지</p><h3>배낭</h3><p>용량별 선택 가이드</p><h3>등산 스틱</h3><p>무릎 보호의 필수품</p><h3>기능성 의류</h3><p>땀 배출과 보온의 균형</p>', 'admin', 543, 41, true, '취미', '등산,아웃도어,초보', '/uploads/images/test_icon_08.png', NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY),
('React 18 새로운 기능 톺아보기', '<p>React 18에서 추가된 주요 기능들</p><h3>Concurrent Features</h3><p>useTransition, useDeferredValue로 더 나은 UX</p><h3>Suspense SSR</h3><p>서버 사이드 렌더링 개선</p><h3>자동 배칭</h3><p>여러 상태 업데이트를 하나로 묶어 최적화</p>', 'admin', 1247, 94, true, '기술', 'React,Frontend,JavaScript', '/uploads/images/test_icon_09.png', NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY),
('서울 야경 명소 추천', '<p>서울의 아름다운 야경을 볼 수 있는 곳</p><h3>남산타워</h3><p>서울 시내 전경</p><h3>반포 한강공원</h3><p>달빛무지개분수쇼</p><h3>북악스카이웨이</h3><p>청와대 야경</p><h3>롯데월드타워 서울스카이</h3><p>555m 높이에서 바라보는 파노라마</p>', 'admin', 867, 58, true, '여행', '서울,야경,데이트', '/uploads/images/test_icon_10.png', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
('Spring Security JWT 인증 구현', '<p>JWT 기반 인증 시스템 구축 가이드</p><h3>JwtAuthenticationFilter 구성</h3><pre>public class JwtAuthenticationFilter extends OncePerRequestFilter {\n  // 토큰 검증 로직\n}</pre><h3>토큰 발급</h3><p>Access Token과 Refresh Token 전략</p><h3>보안 고려사항</h3><p>토큰 저장소와 만료 처리</p>', 'admin', 1834, 112, true, '기술', 'Spring Security,JWT,인증', '/uploads/images/test_icon_11.png', NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
('홈카페 에스프레소 머신 추천', '<p>집에서 즐기는 카페 품질의 커피</p><h3>입문용 (50만원대)</h3><p>가성비 좋은 반자동 머신</p><h3>중급용 (100만원대)</h3><p>PID 온도 조절 기능</p><h3>고급용 (200만원 이상)</h3><p>듀얼 보일러 시스템</p><h3>그라인더</h3><p>머신만큼 중요한 선택</p>', 'admin', 692, 47, true, '취미', '커피,홈카페,에스프레소', '/uploads/images/test_icon_12.png', NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
('MySQL 성능 튜닝 기초', '<p>데이터베이스 성능 최적화 방법</p><h3>인덱스 전략</h3><p>B-Tree vs Hash 인덱스</p><h3>쿼리 플랜 분석</h3><pre>EXPLAIN SELECT * FROM posts WHERE category = "기술";</pre><h3>슬로우쿼리 로그</h3><p>병목 지점 찾기</p>', 'admin', 430, 26, true, '기술', 'MySQL,DB,성능', '/uploads/images/test_icon_13.png', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
('캠핑 초보 체크리스트', '<p>첫 캠핑 떠나기 전 필수 준비물</p><h3>텐트 & 매트</h3><p>계절별 선택 가이드</p><h3>화로 & 버너</h3><p>취사 도구 세트</p><h3>랜턴 & 조명</h3><p>밤을 밝히는 필수품</p><h3>테이블 & 의자</h3><p>편안한 캠핑의 시작</p>', 'admin', 301, 19, true, '취미', '캠핑,아웃도어,장비', '/uploads/images/test_icon_14.png', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
('Next.js 15 업그레이드 가이드', '<p>Next.js 15로 마이그레이션 가이드</p><h3>App Router</h3><p>pages에서 app으로 전환</p><h3>React Server Components</h3><p>서버 컴포넌트 활용법</p><h3>캐시 전략</h3><p>fetch cache 옵션 이해하기</p><h3>번들 최적화</h3><p>Tree Shaking과 Code Splitting</p>', 'admin', 510, 33, true, '기술', 'Next.js,Frontend,웹', '/uploads/images/test_icon_15.png', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
('Kotlin으로 Spring 개발 시작하기', '<p>Kotlin과 Spring을 함께 사용해 생산적인 백엔드 개발을 시작해봅니다.</p><ul><li>코틀린 문법 기초</li><li>Spring WebFlux와의 궁합</li><li>null-safety 장점</li></ul>', 'admin', 378, 29, true, '기술', 'Kotlin,Spring,Backend', '/uploads/images/test_icon_16.png', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
('도시 야경 사진 촬영 팁', '<p>야간 촬영을 위한 기본 세팅과 구도 잡는 법을 소개합니다.</p><ul><li>삼각대와 셔터 속도</li><li>ISO와 노이즈 관리</li><li>RAW 촬영의 장점</li></ul>', 'admin', 455, 34, true, '취미', '사진,야경,카메라', '/uploads/images/test_icon_17.png', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
('주말 드라이브 코스 추천', '<p>도심 근교에서 즐기는 드라이브 코스를 정리했습니다.</p><h3>북부 코스</h3><p>산책과 전망 좋음</p><h3>남부 코스</h3><p>바닷길을 따라 여유롭게</p>', 'admin', 512, 36, true, '여행', '드라이브,근교,힐링', '/uploads/images/test_icon_18.png', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
('IntelliJ 단축키 모음', '<p>개발 생산성을 높여주는 IntelliJ IDEA 단축키를 정리했습니다.</p><ul><li>탐색/코드 이동</li><li>리팩토링</li><li>디버깅</li></ul>', 'admin', 621, 42, true, '기술', 'IntelliJ,생산성,IDE', '/uploads/images/test_icon_19.png', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
('미니멀 라이프 시작 가이드', '<p>소소하지만 확실한 행복을 위한 미니멀 라이프 실천 팁.</p><ul><li>물건 줄이기</li><li>시간 관리</li><li>재정 정리</li></ul>', 'admin', 288, 21, true, '라이프', '미니멀리즘,정리,생활', '/uploads/images/test_icon_20.png', NOW(), NOW());

-- 테스트용 스토리 3개 추가 (실제 비디오 파일 연결)
INSERT INTO stories (title, description, author, video_url, category, tags, view_count, like_count, is_public, created_at, updated_at)
VALUES
('카메라 DSLR 테스트 촬영', '고화질 DSLR 카메라로 촬영한 테스트 영상입니다. 다양한 각도와 조명에서의 화질을 확인할 수 있습니다.', 'admin', '/uploads/videos/test_camere_DSLR.mp4', '기술', '카메라,DSLR,촬영', 156, 23, true, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
('할로윈 호박 장식 만들기', '할로윈을 맞아 호박으로 멋진 장식을 만드는 과정을 담았습니다. 초보자도 쉽게 따라할 수 있는 가이드입니다.', 'admin', '/uploads/videos/test_halloween_pumpkin.mp4', '브이로그', '할로윈,DIY,장식', 289, 45, true, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
('창가에서 즐기는 커피 타임', '창밖 풍경을 바라보며 즐기는 여유로운 커피 한 잔. 일상 속 작은 행복을 담았습니다.', 'admin', '/uploads/videos/test_window_coffee.mp4', '브이로그', '커피,힐링,일상', 412, 67, true, NOW(), NOW());

-- 테스트용 공지사항 13개 추가 (단순 텍스트, 관리자만 작성/수정)
INSERT INTO notices (title, content, author, view_count, created_at, updated_at) VALUES
('사이트 점검 안내', '안정적인 서비스 제공을 위해 이번 주 토요일 02:00~04:00에 서버 점검을 진행합니다. 이용에 불편을 드려 죄송합니다.', 'admin', 120, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY),
('개인정보 처리방침 업데이트', '관련 법령 개정에 따라 개인정보 처리방침이 일부 변경되었습니다. 자세한 내용은 공지 전문을 확인해주세요.', 'admin', 85, NOW() - INTERVAL 27 DAY, NOW() - INTERVAL 27 DAY),
('신규 메뉴 오픈 안내', '상단 네비게이션에 스토리 메뉴가 추가되었습니다. 모바일에서도 편리하게 이용하실 수 있습니다.', 'admin', 64, NOW() - INTERVAL 24 DAY, NOW() - INTERVAL 24 DAY),
('서비스 속도 개선 공지', '캐시 정책 조정과 이미지 최적화를 통해 페이지 로딩 속도를 개선했습니다.', 'admin', 142, NOW() - INTERVAL 21 DAY, NOW() - INTERVAL 21 DAY),
('버그 수정 내역 안내', '일부 브라우저에서 로그인 후 리다이렉트가 정상 동작하지 않던 문제를 포함해 여러 버그를 수정했습니다.', 'admin', 58, NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 18 DAY),
('정기 점검 완료 보고', '예정된 정기 점검이 정상적으로 완료되었습니다. 감사합니다.', 'admin', 77, NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 16 DAY),
('스팸 계정 차단 안내', '비정상 활동이 탐지된 스팸 계정 일부를 차단 조치하였습니다.', 'admin', 91, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY),
('문의 게시판 운영 시간', '평일 10:00~18:00에 접수된 문의는 당일 응답을 목표로 처리하고 있습니다.', 'admin', 33, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY),
('모바일 최적화 적용', '모바일 환경에서의 가독성과 터치 영역을 개선했습니다.', 'admin', 68, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
('썸네일 업로드 용량 상향', '썸네일 업로드 용량 제한이 5MB에서 10MB로 상향 조정되었습니다.', 'admin', 52, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
('보안 업데이트 적용', '취약점 개선을 위한 보안 업데이트가 적용되었으며 사용 중 영향은 없습니다.', 'admin', 110, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
('서비스 장애 복구 안내', '금일 새벽 간헐적 접속 장애가 발생했으나 현재는 정상화되었습니다. 불편을 드려 죄송합니다.', 'admin', 173, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
('연휴 운영 안내', '공휴일 기간 동안 고객센터 응대가 제한됩니다. 급한 문의는 이메일로 남겨주세요.', 'admin', 41, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);
