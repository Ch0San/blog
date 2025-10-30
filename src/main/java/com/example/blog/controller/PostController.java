package com.example.blog.controller;

import com.example.blog.domain.Comment;
import com.example.blog.domain.Post;
import com.example.blog.service.CommentService;
import com.example.blog.service.PostLikeService;
import com.example.blog.service.CommentLikeService;
import com.example.blog.service.PostService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 게시글 목록 컨트롤러
 */
@Controller
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    private final PostLikeService postLikeService;
    private final CommentLikeService commentLikeService;

    private static final String TEMP_UPLOADS_KEY = "tempUploadedImages";

    public PostController(PostService postService, CommentService commentService, PostLikeService postLikeService,
            CommentLikeService commentLikeService) {
        this.postService = postService;
        this.commentService = commentService;
        this.postLikeService = postLikeService;
        this.commentLikeService = commentLikeService;
    }

    /**
     * 본문 HTML에서 실제 사용된 /uploads/images 경로 추출
     */
    private Set<String> extractUsedImageUrls(String content) {
        Set<String> urls = new HashSet<>();
        if (content == null || content.isEmpty()) {
            return urls;
        }

        // src="/uploads/images/xxx.jpg" 패턴 찾기
        Pattern pattern = Pattern.compile("src=[\"']([^\"']*?/uploads/images/[^\"']+)[\"']");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            urls.add(matcher.group(1));
        }

        return urls;
    }

    /**
     * 임시 업로드 이미지 정리: 실제 사용되지 않은 이미지 삭제
     */
    private void cleanupTempUploads(HttpSession session, String content) {
        @SuppressWarnings("unchecked")
        Set<String> tempUploads = (Set<String>) session.getAttribute(TEMP_UPLOADS_KEY);
        if (tempUploads == null || tempUploads.isEmpty()) {
            return;
        }

        // 본문에서 실제 사용된 이미지 URL 추출
        Set<String> usedUrls = extractUsedImageUrls(content);

        // 사용되지 않은 이미지 삭제
        for (String tempUrl : tempUploads) {
            if (!usedUrls.contains(tempUrl)) {
                try {
                    String relativePath = tempUrl.startsWith("/") ? tempUrl.substring(1) : tempUrl;
                    Path filePath = Paths.get(relativePath);
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                    }
                } catch (Exception e) {
                    // 개별 파일 삭제 실패는 무시
                }
            }
        }

        // 세션에서 임시 목록 제거
        session.removeAttribute(TEMP_UPLOADS_KEY);
    }

    // HTML 태그 제거 유틸리티 메서드
    private String stripHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "").trim();
    }

    // /posts?page=0 기반 (0부터 시작). 뷰에서는 1-based로 표시할 수 있음
    @GetMapping("/posts")
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String category,
            Model model) {
        int size = 10; // 페이지 당 항목 수
        Page<Post> posts;

        if (category != null && !category.isEmpty()) {
            posts = postService.getPostsByCategory(category, page, size);
        } else {
            posts = postService.getPosts(page, size);
        }

        // 각 게시글의 content에서 HTML 태그 제거
        posts.getContent().forEach(post -> {
            if (post.getContent() != null) {
                String plainText = stripHtmlTags(post.getContent());
                post.setContent(plainText);
            }
        });

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("page", posts);
        model.addAttribute("currentCategory", category);
        return "posts/list";
    }

    // 게시글 검색
    @GetMapping("/posts/search")
    public String search(
            @RequestParam String field,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        int size = 10;
        Page<Post> posts;

        switch (field) {
            case "title" -> posts = postService.searchByTitle(q != null ? q : "", page, size);
            case "content" -> posts = postService.searchByContent(q != null ? q : "", page, size);
            case "tags" -> posts = postService.searchByTags(q != null ? q : "", page, size);
            default -> posts = postService.getPosts(page, size);
        }

        // 각 게시글의 content에서 HTML 태그 제거
        posts.getContent().forEach(post -> {
            if (post.getContent() != null) {
                String plainText = stripHtmlTags(post.getContent());
                post.setContent(plainText);
            }
        });

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("page", posts);
        model.addAttribute("currentCategory", null);
        model.addAttribute("searchField", field);
        model.addAttribute("searchQuery", q);
        return "posts/list";
    }

    // 게시글 상세보기
    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        Post post = postService.getPostByIdAndIncrementViewCount(id);
        if (post == null) {
            return "redirect:/posts";
        }

        // 본문에 사용된 이미지를 첨부파일 목록에서 제외
        if (post.getImages() != null && !post.getImages().isEmpty() && post.getContent() != null) {
            String content = post.getContent();
            // 본문에 포함되지 않은 이미지만 필터링 (첨부파일만)
            java.util.List<com.example.blog.domain.PostImage> attachments = post.getImages().stream()
                    .filter(img -> !content.contains(img.getImageUrl()))
                    .toList();
            model.addAttribute("attachments", attachments);
        } else {
            model.addAttribute("attachments", java.util.Collections.emptyList());
        }

        // 댓글 목록 조회
        List<Comment> comments = commentService.getCommentsByPostId(id);

        // 좋아요 여부 확인 (로그인한 경우에만)
        boolean isLiked = false;
        java.util.Set<Long> likedCommentIds = java.util.Collections.emptySet();
        if (authentication != null && authentication.isAuthenticated()) {
            isLiked = postLikeService.isLiked(id, authentication.getName());
            likedCommentIds = commentLikeService.getLikedCommentIdsForPost(id, authentication.getName());
        }

        // 최신글 5개 조회 (현재 글 제외)
        List<Post> recentPosts = postService.getRecentPosts(5).stream()
                .filter(p -> !p.getId().equals(id))
                .limit(5)
                .toList();

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("likedCommentIds", likedCommentIds);
        model.addAttribute("recentPosts", recentPosts);
        return "posts/detail";
    }

    // 글쓰기 페이지
    @GetMapping("/posts/write")
    public String writeForm() {
        return "posts/write";
    }

    // 글 저장
    @PostMapping(value = "/posts/write", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String write(
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String content,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) MultipartFile thumbnailFile,
            @RequestParam(name = "thumbnailUrl", required = false) String thumbnailUrlParam,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(value = "imageFiles", required = false) java.util.List<MultipartFile> imageFiles,
            @RequestParam(value = "files", required = false) java.util.List<MultipartFile> files,
            @RequestParam(value = "imageUrls", required = false) String imageUrls,
            @RequestParam(required = false, defaultValue = "false") boolean isPublic,
            HttpSession session) {
        Post post = new Post();
        post.setTitle(title);
        post.setAuthor(author);
        post.setContent(content);
        post.setCategory(category);
        post.setTags(tags);


        // 썸네일 결정: 업로드 파일 > 클라이언트 선택 URL(hidden input)
        String chosenThumbnail = null;
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            chosenThumbnail = saveThumbnailFile(thumbnailFile);
        } else if (thumbnailUrlParam != null && !thumbnailUrlParam.isBlank()) {
            chosenThumbnail = normalizeWebPath(thumbnailUrlParam);
        }

        // 다중 이미지 업로드(각 10MB 이하). 첫 이미지로 대표 섬네일 설정, 나머지는 PostImage로 저장
        try {
            java.nio.file.Path uploadRoot = java.nio.file.Paths.get("uploads", "images");
            java.nio.file.Files.createDirectories(uploadRoot);

            java.util.List<String> savedPaths = new java.util.ArrayList<>();
            java.util.List<String> savedFilePaths = new java.util.ArrayList<>();
            if (imageFiles != null) {
                int idx = 0;
                for (MultipartFile file : imageFiles) {
                    if (file == null || file.isEmpty())
                        continue;
                    if (file.getSize() > 10L * 1024 * 1024) {
                        return "redirect:/posts/write?error=IMAGE_TOO_LARGE";
                    }
                    String original = file.getOriginalFilename();
                    String ext = (original != null && original.contains("."))
                            ? original.substring(original.lastIndexOf('.'))
                            : "";
                    String filename = java.time.LocalDate.now() + "-" + java.util.UUID.randomUUID() + ext;
                    java.nio.file.Path target = uploadRoot.resolve(filename);
                    try (java.io.InputStream in = file.getInputStream()) {
                        java.nio.file.Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    String webPath = "/uploads/images/" + filename;
                    savedPaths.add(webPath);
                    // PostImage 객체 추가 (첨부파일만)
                    com.example.blog.domain.PostImage pi = new com.example.blog.domain.PostImage();
                    pi.setPost(post);
                    pi.setImageUrl(webPath);
                    pi.setCaption(original); // 원본 파일명 저장
                    pi.setSortOrder(idx++);
                    post.getImages().add(pi);
                }
            }
            // 일반 파일 업로드 -> uploads/files 저장 후 콤마로 files_url에 저장
            if (files != null) {
                java.nio.file.Path filesRoot = java.nio.file.Paths.get("uploads", "files");
                java.nio.file.Files.createDirectories(filesRoot);
                for (MultipartFile f : files) {
                    if (f == null || f.isEmpty()) continue;
                    if (f.getSize() > 10L * 1024 * 1024) {
                        return "redirect:/posts/write?error=FILE_TOO_LARGE";
                    }
                    String original = f.getOriginalFilename();
                    String ext = (original != null && original.contains("."))
                            ? original.substring(original.lastIndexOf('.'))
                            : "";
                    String filename = java.time.LocalDate.now() + "-" + java.util.UUID.randomUUID() + ext;
                    java.nio.file.Path target = filesRoot.resolve(filename);
                    try (java.io.InputStream in = f.getInputStream()) {
                        java.nio.file.Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    String webPath = "/uploads/files/" + filename;
                    savedFilePaths.add(webPath);
                }
                if (!savedFilePaths.isEmpty()) {
                    post.setFilesUrl(String.join(",", savedFilePaths));
                }
            }
            // imageUrls는 본문에 삽입된 이미지이므로 PostImage에 추가하지 않음 (첨부파일 아님)

            // 대표 이미지 설정: 썸네일 파일 > 첫 번째 첨부 이미지 순서
            if (chosenThumbnail != null && !chosenThumbnail.isBlank()) {
                post.setThumbnailUrl(chosenThumbnail);
            } else if (!savedPaths.isEmpty()) {
                post.setThumbnailUrl(savedPaths.get(0));
            }
        } catch (Exception e) {
            return "redirect:/posts/write?error=IMAGE_UPLOAD_FAIL";
        }

        post.setVideoUrl(normalizeWebPath(videoUrl));
        post.setPublic(isPublic);

        postService.savePost(post);

        // 임시 업로드 이미지 정리 (본문에 사용되지 않은 이미지 삭제)
        cleanupTempUploads(session, content);

        return "redirect:/posts";
    }

    // 글 수정 페이지
    @GetMapping("/posts/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return "redirect:/posts";
        }
        model.addAttribute("post", post);
        return "posts/edit";
    }

    // 글 수정 처리
    @PostMapping("/posts/edit/{id}")
    public String edit(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String content,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) MultipartFile thumbnailFile,
            @RequestParam(name = "thumbnailUrl", required = false) String thumbnailUrlParam,
            @RequestParam(required = false) Boolean deleteThumbnail,
            @RequestParam(value = "deleteImageIds", required = false) java.util.List<Long> deleteImageIds,
            @RequestParam(value = "files", required = false) java.util.List<MultipartFile> files,
            @RequestParam(value = "imageFiles", required = false) java.util.List<MultipartFile> imageFiles,
            @RequestParam(value = "imageUrls", required = false) String imageUrls,
            @RequestParam(required = false, defaultValue = "false") boolean isPublic,
            HttpSession session) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return "redirect:/posts";
        }

        post.setTitle(title);
        post.setAuthor(author);
        post.setContent(content);
        post.setCategory(category);
        post.setTags(tags);

        // 썸네일 삭제 체크박스가 체크된 경우
        if (Boolean.TRUE.equals(deleteThumbnail)) {
            String existingThumbnail = post.getThumbnailUrl();
            deleteImageFileByUrl(existingThumbnail);
            post.setThumbnailUrl(null);
        }
        // 새 썸네일 이미지가 업로드된 경우 (삭제 체크박스가 없을 때만)
        else if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            // 기존 썸네일이 있으면 삭제
            String existingThumbnail = post.getThumbnailUrl();
            deleteImageFileByUrl(existingThumbnail);

            String newThumb = saveThumbnailFile(thumbnailFile);
            post.setThumbnailUrl(newThumb);
        }
        else if (thumbnailUrlParam != null && !thumbnailUrlParam.isBlank()) {
            String existingThumbnail = post.getThumbnailUrl();
            deleteImageFileByUrl(existingThumbnail);
            post.setThumbnailUrl(normalizeWebPath(thumbnailUrlParam));
        }

        // 새로운 이미지 업로드 처리
        try {
            java.nio.file.Path uploadRoot = java.nio.file.Paths.get("uploads", "images");
            java.nio.file.Files.createDirectories(uploadRoot);

            java.util.List<String> savedPaths = new java.util.ArrayList<>();
            java.util.List<String> savedFilePaths = new java.util.ArrayList<>();

            // 기존 이미지 개수 확인
            int startIdx = post.getImages() != null ? post.getImages().size() : 0;

            if (imageFiles != null) {
                int idx = startIdx;
                for (MultipartFile file : imageFiles) {
                    if (file == null || file.isEmpty())
                        continue;
                    if (file.getSize() > 10L * 1024 * 1024) {
                        return "redirect:/posts/edit/" + id + "?error=IMAGE_TOO_LARGE";
                    }
                    String original = file.getOriginalFilename();
                    String ext = (original != null && original.contains("."))
                            ? original.substring(original.lastIndexOf('.'))
                            : "";
                    String filename = java.time.LocalDate.now() + "-" + java.util.UUID.randomUUID() + ext;
                    java.nio.file.Path target = uploadRoot.resolve(filename);
                    try (java.io.InputStream in = file.getInputStream()) {
                        java.nio.file.Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    String webPath = "/uploads/images/" + filename;
                    savedPaths.add(webPath);
                    // PostImage 객체 추가 (첨부파일만)
                    com.example.blog.domain.PostImage pi = new com.example.blog.domain.PostImage();
                    pi.setPost(post);
                    pi.setImageUrl(webPath);
                    pi.setCaption(original); // 원본 파일명 저장
                    pi.setSortOrder(idx++);
                    post.getImages().add(pi);
                }
            }

            // 일반 파일 업로드 (uploads/files) -> 기존 값에 추가
            if (files != null) {
                java.nio.file.Path filesRoot = java.nio.file.Paths.get("uploads", "files");
                java.nio.file.Files.createDirectories(filesRoot);
                for (MultipartFile f : files) {
                    if (f == null || f.isEmpty()) continue;
                    if (f.getSize() > 10L * 1024 * 1024) {
                        return "redirect:/posts/edit/" + id + "?error=FILE_TOO_LARGE";
                    }
                    String original = f.getOriginalFilename();
                    String ext = (original != null && original.contains("."))
                            ? original.substring(original.lastIndexOf('.'))
                            : "";
                    String filename = java.time.LocalDate.now() + "-" + java.util.UUID.randomUUID() + ext;
                    java.nio.file.Path target = filesRoot.resolve(filename);
                    try (java.io.InputStream in = f.getInputStream()) {
                        java.nio.file.Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    String webPath = "/uploads/files/" + filename;
                    savedFilePaths.add(webPath);
                }
                if (!savedFilePaths.isEmpty()) {
                    String existing = post.getFilesUrl();
                    post.setFilesUrl(existing == null || existing.isBlank()
                            ? String.join(",", savedFilePaths)
                            : existing + "," + String.join(",", savedFilePaths));
                }
            }

            // imageUrls는 본문에 삽입된 이미지이므로 PostImage에 추가하지 않음 (첨부파일 아님)

            // 썸네일이 삭제되었고 새 첨부 이미지가 있으면 첫 번째 이미지를 썸네일로
            if (!Boolean.TRUE.equals(deleteThumbnail)
                    && (post.getThumbnailUrl() == null || post.getThumbnailUrl().isBlank())
                    && !savedPaths.isEmpty()) {
                post.setThumbnailUrl(savedPaths.get(0));
            }

        } catch (Exception e) {
            return "redirect:/posts/edit/" + id + "?error=IMAGE_UPLOAD_FAIL";
        }

        post.setPublic(isPublic);

        postService.savePost(post);

        // 임시 업로드 이미지 정리 (본문에 사용되지 않은 이미지 삭제)
        cleanupTempUploads(session, content);

        return "redirect:/posts/" + id;
    }

    // 글 삭제
    @PostMapping("/posts/delete/{id}")
    public String delete(@PathVariable Long id) {
        postService.deletePost(id);
        return "redirect:/posts";
    }

    // 파일 다운로드
    @GetMapping("/posts/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filename) {
        try {
            // uploads 디렉토리 경로
            Path uploadPath = Paths.get("uploads/images").toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();

            // 경로 검증 (디렉토리 트래버설 공격 방지)
            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // 파일의 Content-Type 감지
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // 원본 파일명 추출 (UUID 제거)
            String originalFilename = filename;
            if (filename.contains("-") && filename.length() > 36) {
                // UUID 부분 제거 시도 (예: 2025-10-27-uuid-filename.jpg -> filename.jpg)
                int lastDashIndex = filename.lastIndexOf('-');
                if (lastDashIndex > 0) {
                    originalFilename = filename.substring(lastDashIndex + 1);
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + originalFilename + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 파일 다운로드 (uploads/files)
    @GetMapping("/posts/file/download")
    public ResponseEntity<Resource> downloadAttachment(@RequestParam String filename) {
        try {
            Path uploadPath = Paths.get("uploads/files").toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();
            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.badRequest().build();
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";
            String originalFilename = filename;
            if (filename.contains("-") && filename.length() > 36) {
                int lastDashIndex = filename.lastIndexOf('-');
                if (lastDashIndex > 0) {
                    originalFilename = filename.substring(lastDashIndex + 1);
                }
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 업로드 경로 정규화: 'uploads/...'로 시작하면 앞에 슬래시를 붙여 '/uploads/...' 형태로 변환
    // 외부 URL(http/https)이나 빈 값은 그대로 둔다.
    private String normalizeWebPath(String path) {
        if (path == null)
            return null;
        String trimmed = path.trim();
        if (trimmed.isEmpty())
            return trimmed;
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("/uploads/"))
            return trimmed;
        if (trimmed.startsWith("uploads/"))
            return "/" + trimmed;
        return trimmed;
    }

    // 썸네일 이미지 파일 저장 메서드
    private String saveThumbnailFile(MultipartFile file) {
        try {
            // 업로드 디렉토리 생성
            String uploadDir = "uploads/images/";
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = "post_thumb_" + UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path filePath = Paths.get(uploadDir + uniqueFilename);
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // 웹에서 접근 가능한 URL 반환
            return "/uploads/images/" + uniqueFilename;

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // 이미지 파일 삭제 메서드 (URL 기반, uploads/images 하위만 허용)
    private void deleteImageFileByUrl(String existingUrl) {
        if (existingUrl == null || existingUrl.isBlank())
            return;
        try {
            if (existingUrl.startsWith("/uploads/images/")) {
                String existingName = existingUrl.substring(existingUrl.lastIndexOf('/') + 1);
                Path uploadsDir = Paths.get("uploads/images").toAbsolutePath().normalize();
                Path existingPath = uploadsDir.resolve(existingName).normalize();
                if (existingPath.startsWith(uploadsDir)) {
                    Files.deleteIfExists(existingPath);
                }
            }
        } catch (Exception ex) {
            // 삭제 실패 시에도 흐름은 계속
            ex.printStackTrace();
        }
    }
}
