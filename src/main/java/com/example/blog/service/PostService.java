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
@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Page<Post> getPosts(int page, int size) {
        return postRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 카테고리별 게시글 조회
    public Page<Post> getPostsByCategory(String category, int page, int size) {
        return postRepository.findByCategory(category,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public Post getPostById(Long id) {
        return postRepository.findByIdWithImages(id).orElse(null);
    }

    // 조회수 증가와 함께 게시글 조회
    @Transactional
    public Post getPostByIdAndIncrementViewCount(Long id) {
        Post post = postRepository.findByIdWithImages(id).orElse(null);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
        }
        return post;
    }

    @Transactional
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    // 검색: 제목
    public Page<Post> searchByTitle(String keyword, int page, int size) {
        return postRepository.findByTitleContainingIgnoreCase(keyword,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 검색: 태그
    public Page<Post> searchByTags(String keyword, int page, int size) {
        return postRepository.findByTagsContainingIgnoreCase(keyword,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 검색: 내용
    public Page<Post> searchByContent(String keyword, int page, int size) {
        return postRepository.findByContentContainingIgnoreCase(keyword,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 검색: 작성일(하루)
    public Page<Post> searchByDate(java.time.LocalDate date, int page, int size) {
        java.time.LocalDateTime start = date.atStartOfDay();
        java.time.LocalDateTime end = date.atTime(23, 59, 59, 999000000);
        return postRepository.findByCreatedAtBetween(start, end,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // 검색: 작성일 범위
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
    public List<Post> getPopularPosts(int limit) {
        return postRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "viewCount"))).getContent();
    }

    // 최신글 조회 (작성일 순)
    public List<Post> getRecentPosts(int limit) {
        return postRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }

    // 전체 게시글 수
    public long getTotalPostCount() {
        return postRepository.count();
    }
}
