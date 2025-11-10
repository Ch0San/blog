package com.example.blog.service;

import com.example.blog.domain.Post;
import com.example.blog.domain.PostImage;
import com.example.blog.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 게시글 조회 서비스
 * 기본적으로 최신순 정렬, 페이지 사이즈는 컨트롤러에서 전달 (기본 10)
 */
/**
 * 게시글(Post) 비즈니스 로직 서비스.
 *
 * 기본 정렬은 최신 생성일 내림차순이며, 페이징 크기는 호출부에서 전달합니다.
 * 로컬 업로드 경로(`/uploads/...`)에 저장된 첨부파일 정리 로직을 포함합니다.
 */
@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * 전체 게시글 페이지를 조회합니다.
     *
     * @param page 0부터 시작하는 페이지 번호
     * @param size 페이지 크기
     * @return 게시글 페이지(최신 생성일 내림차순)
     */
    public Page<Post> getPosts(int page, int size) {
        return postRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 카테고리별 게시글 조회
    /**
     * 카테고리로 게시글 페이지를 조회합니다.
     *
     * @param category 카테고리명
     * @param page     0부터 시작하는 페이지 번호
     * @param size     페이지 크기
     * @return 게시글 페이지(최신 생성일 내림차순)
     */
    public Page<Post> getPostsByCategory(String category, int page, int size) {
        return postRepository.findByCategory(category,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    /**
     * 게시글을 ID로 조회합니다. 관련 이미지까지 함께 로딩합니다.
     *
     * @param id 게시글 식별자
     * @return 게시글 또는 null(없음)
     */
    public Post getPostById(Long id) {
        return postRepository.findByIdWithImages(id).orElse(null);
    }

    // 조회수 증가와 함께 게시글 조회
    /**
     * 게시글을 조회하고 조회수를 1 증가시킵니다.
     *
     * @param id 게시글 식별자
     * @return 게시글 또는 null(없음)
     */
    @Transactional
    public Post getPostByIdAndIncrementViewCount(Long id) {
        Post post = postRepository.findByIdWithImages(id).orElse(null);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
        }
        return post;
    }

    /**
     * 게시글을 저장합니다.
     *
     * @param post 저장할 게시글 엔티티
     * @return 저장된 게시글
     */
    @Transactional
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    // 검색: 제목
    /**
     * 제목으로 게시글을 검색합니다(대소문자 무시, 부분일치).
     *
     * @param keyword 검색어(빈 문자열 허용)
     * @param page    0부터 시작하는 페이지 번호
     * @param size    페이지 크기
     * @return 검색 결과 페이지
     */
    public Page<Post> searchByTitle(String keyword, int page, int size) {
        return postRepository.findByTitleContainingIgnoreCase(keyword,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 검색: 태그
    /**
     * 태그로 게시글을 검색합니다(대소문자 무시, 부분일치).
     *
     * @param keyword 검색어(빈 문자열 허용)
     * @param page    0부터 시작하는 페이지 번호
     * @param size    페이지 크기
     * @return 검색 결과 페이지
     */
    public Page<Post> searchByTags(String keyword, int page, int size) {
        return postRepository.findByTagsContainingIgnoreCase(keyword,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 검색: 내용
    /**
     * 본문 내용으로 게시글을 검색합니다(대소문자 무시, 부분일치).
     *
     * @param keyword 검색어(빈 문자열 허용)
     * @param page    0부터 시작하는 페이지 번호
     * @param size    페이지 크기
     * @return 검색 결과 페이지
     */
    public Page<Post> searchByContent(String keyword, int page, int size) {
        return postRepository.findByContentContainingIgnoreCase(keyword,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 검색: 작성일(하루)
    /**
     * 특정 날짜에 생성된 게시글을 검색합니다(자정~23:59:59.999 범위).
     *
     * @param date 날짜(필수)
     * @param page 0부터 시작하는 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과 페이지
     */
    public Page<Post> searchByDate(java.time.LocalDate date, int page, int size) {
        java.time.LocalDateTime start = date.atStartOfDay();
        java.time.LocalDateTime end = date.atTime(23, 59, 59, 999000000);
        return postRepository.findByCreatedAtBetween(start, end,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 검색: 작성일 범위
    /**
     * 날짜 범위로 게시글을 검색합니다.
     *
     * @param startDate 시작일(없으면 endDate와 동일하게 보정)
     * @param endDate   종료일(없으면 startDate와 동일하게 보정)
     * @param page      0부터 시작하는 페이지 번호
     * @param size      페이지 크기
     * @return 검색 결과 페이지
     */
    public Page<Post> searchByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate, int page,
            int size) {
        if (startDate == null && endDate == null) {
            startDate = java.time.LocalDate.now();
            endDate = startDate;
        } else if (startDate == null) {
            startDate = endDate;
        } else if (endDate == null) {
            endDate = startDate;
        }
        java.time.LocalDateTime start = startDate.atStartOfDay();
        java.time.LocalDateTime end = endDate.atTime(23, 59, 59, 999000000);
        return postRepository.findByCreatedAtBetween(start, end,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 게시글 삭제 (+ 로컬 파일 정리)
    /**
     * 게시글을 삭제합니다. 로컬 업로드 경로에 저장된 첨부파일(썸네일/비디오/본문 내 포함 리소스)도 함께 정리합니다.
     *
     * @param id 게시글 식별자
     */
    @Transactional
    public void deletePost(Long id) {
        // 첨부 파일 경로 확보를 위해 먼저 엔티티를 조회
        Post post = postRepository.findByIdWithImages(id).orElse(null);
        if (post != null) {
            // 썸네일이 로컬 업로드 경로라면 삭제
            deleteLocalFileIfUnderUploads(post.getThumbnailUrl());

            // (옵션) 포스트에 동영상 URL 필드가 있고 로컬 업로드 경로라면 삭제
            deleteLocalFileIfUnderUploads(post.getVideoUrl());

            // 개별 첨부 이미지 파일 삭제
            List<PostImage> images = post.getImages();
            if (images != null) {
                for (PostImage img : images) {
                    deleteLocalFileIfUnderUploads(img.getImageUrl());
                }
            }

            // 본문 내 임베드된 업로드 파일(img/video) 경로 정리
            deleteUploadsReferencedInHtml(post.getContent());
        }

        // DB 삭제 (연관 PostImage는 orphanRemoval=true로 함께 삭제됨)
        postRepository.deleteById(id);
    }

    /**
     * `/uploads/...` 하위의 로컬 파일만 안전하게 삭제합니다.
     * 디렉터리 이탈(Path Traversal)을 방지하기 위해 업로드 루트 하위인지 검증합니다.
     *
     * @param url `/uploads/...` 또는 `uploads/...` 형식의 URL/상대경로(그 외는 무시)
     */
    private void deleteLocalFileIfUnderUploads(String url) {
        if (url == null || url.isBlank())
            return;
        // /uploads/... 또는 uploads/... 만 삭제 대상
        String normalizedUrl = url.trim();
        if (!(normalizedUrl.startsWith("/uploads/") || normalizedUrl.startsWith("uploads/"))) {
            return; // 외부 URL 등은 건드리지 않음
        }
        try {
            Path uploadsRoot = Paths.get("uploads").toAbsolutePath().normalize();
            String relative = normalizedUrl.startsWith("/") ? normalizedUrl.substring(1) : normalizedUrl;
            Path target = uploadsRoot.resolve(relative.substring("uploads/".length())).normalize();
            // 안전 체크: uploads 디렉토리 내부인지 확인
            if (!target.startsWith(uploadsRoot))
                return;
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // 파일이 없거나 권한 문제 등 - 로그 생략하고 무시
        }
    }

    /**
     * HTML 본문에서 `/uploads/images` 또는 `/uploads/videos`로 참조되는 리소스 파일을 찾아 삭제합니다.
     * 중복 경로는 한 번만 처리합니다.
     *
     * @param html 게시글 HTML 본문
     */
    private void deleteUploadsReferencedInHtml(String html) {
        if (html == null || html.isBlank())
            return;
        // /uploads/images/... 또는 /uploads/videos/... 패턴을 찾는다
        Pattern p = Pattern.compile("(/uploads/(images|videos)/[^\"'\\s)<>]+)");
        Matcher m = p.matcher(html);
        Set<String> seen = new HashSet<>();
        while (m.find()) {
            String path = m.group(1);
            if (seen.add(path)) {
                deleteLocalFileIfUnderUploads(path);
            }
        }
    }

    // 인기글 조회 (조회수 순)
    /**
     * 인기글을 조회합니다(조회수 내림차순, 상위 N개).
     *
     * @param limit 가져올 개수
     * @return 인기글 목록
     */
    public List<Post> getPopularPosts(int limit) {
        return postRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "viewCount"))).getContent();
    }

    // 최신글 조회 (작성일 순)
    /**
     * 최신글을 조회합니다(생성일 내림차순, 상위 N개).
     *
     * @param limit 가져올 개수
     * @return 최신글 목록
     */
    public List<Post> getRecentPosts(int limit) {
        return postRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }

    // 전체 게시글 수
    /**
     * 전체 게시글 수를 반환합니다.
     *
     * @return 게시글 총 개수
     */
    public long getTotalPostCount() {
        return postRepository.count();
    }
}
