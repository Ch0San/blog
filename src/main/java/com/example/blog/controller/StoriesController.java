package com.example.blog.controller;

import com.example.blog.domain.Story;
import com.example.blog.domain.StoryComment;
import com.example.blog.domain.Member;
import com.example.blog.service.StoryService;
import com.example.blog.service.StoryCommentService;
import com.example.blog.service.MemberService;
import com.example.blog.service.StoryCommentLikeService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * 스토리(Stories) 게시판 컨트롤러
 */
/**
 * 스토리(Stories) 게시판 컨트롤러.
 * 
 * 목록/검색/상세 보기와 스토리 댓글의 생성·수정·삭제,
 * 스토리 생성/수정/삭제 및 첨부파일(영상, 썸네일) 업로드/정리를 담당합니다.
 */
@Controller
public class StoriesController {
    private final StoryService storyService;
    private final StoryCommentService storyCommentService;
    private final MemberService memberService;
    private final StoryCommentLikeService storyCommentLikeService;
    private final com.example.blog.service.StoryLikeService storyLikeService;

    public StoriesController(StoryService storyService, StoryCommentService storyCommentService,
            MemberService memberService, StoryCommentLikeService storyCommentLikeService,
            com.example.blog.service.StoryLikeService storyLikeService) {
        this.storyService = storyService;
        this.storyCommentService = storyCommentService;
        this.memberService = memberService;
        this.storyCommentLikeService = storyCommentLikeService;
        this.storyLikeService = storyLikeService;
    }

    /**
     * 스토리 목록 페이지를 조회합니다.
     * <p>
     * 기본 페이지 크기는 12이며, 카테고리 필터가 지정되면 해당 카테고리로 필터링합니다.
     * </p>
     *
     * @param page      0부터 시작하는 페이지 번호
     * @param category  선택적 카테고리명(없으면 전체)
     * @param model     뷰 렌더링에 사용할 모델
     * @return 목록 뷰 이름(`stories/list`)
     */
    @GetMapping("/stories")
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String category,
            Model model) {
        int size = 12; // 한 페이지에 12개씩 (3x4 그리드)
        Page<Story> stories;

        if (category != null && !category.isEmpty()) {
            stories = storyService.getStoriesByCategory(category, page, size);
        } else {
            stories = storyService.getStories(page, size);
        }

        model.addAttribute("stories", stories.getContent());
        model.addAttribute("page", stories);
        model.addAttribute("currentCategory", category);
        return "stories/list";
    }

    // 스토리 검색
    /**
     * 스토리 검색 결과 목록을 조회합니다.
     *
     * @param field 검색 필드(`title`, `content`, `tags` 중 하나)
     * @param q     검색어(없으면 빈 문자열)
     * @param page  0부터 시작하는 페이지 번호
     * @param model 뷰 렌더링에 사용할 모델
     * @return 목록 뷰 이름(`stories/list`)
     */
    @GetMapping("/stories/search")
    public String search(
            @RequestParam String field,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        int size = 12; // 그리드에 맞춘 페이지 사이즈
        Page<Story> stories;

        switch (field) {
            case "title" -> stories = storyService.searchByTitle(q != null ? q : "", page, size);
            case "content" -> stories = storyService.searchByContent(q != null ? q : "", page, size);
            case "tags" -> stories = storyService.searchByTags(q != null ? q : "", page, size);
            default -> stories = storyService.getStories(page, size);
        }

        model.addAttribute("stories", stories.getContent());
        model.addAttribute("page", stories);
        model.addAttribute("currentCategory", null);
        model.addAttribute("searchField", field);
        model.addAttribute("searchQuery", q);
        return "stories/list";
    }

    // 스토리 상세보기
    /**
     * 스토리 상세 페이지를 조회합니다. 조회수는 1 증가합니다.
     * <p>
     * 인증된 사용자라면 사용자가 좋아요한 댓글/스토리 여부 정보를 함께 전달합니다.
     * </p>
     *
     * @param id             스토리 식별자
     * @param model          뷰 렌더링용 모델
     * @param authentication 스프링 시큐리티 인증 정보(선택)
     * @return 상세 뷰 이름(`stories/detail`), 존재하지 않으면 목록으로 리다이렉트
     */
    @GetMapping("/stories/{id}")
    public String detail(@PathVariable Long id, Model model,
            org.springframework.security.core.Authentication authentication) {
        Story story = storyService.getStoryByIdAndIncrementViewCount(id);
        if (story == null) {
            return "redirect:/stories";
        }

        // 댓글 목록 조회
        List<StoryComment> comments = storyCommentService.getCommentsByStoryId(id);
        long commentCount = storyCommentService.getCommentCount(id);
        // 로그인 사용자의 댓글 좋아요 목록 및 스토리 좋아요 여부
        java.util.Set<Long> likedCommentIds = java.util.Collections.emptySet();
        boolean isStoryLiked = false;
        if (authentication != null && authentication.isAuthenticated()) {
            likedCommentIds = storyCommentLikeService.getLikedCommentIdsForStory(id, authentication.getName());
            isStoryLiked = storyLikeService.isLiked(id, authentication.getName());
        }

        model.addAttribute("story", story);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("likedCommentIds", likedCommentIds);
        model.addAttribute("isStoryLiked", isStoryLiked);
        return "stories/detail";
    }

    // 스토리 댓글 작성
    /**
     * 스토리에 댓글을 생성합니다.
     *
     * @param id           스토리 식별자
     * @param content      댓글 내용(필수)
     * @param userDetails  인증 사용자 정보(필수)
     * @return 상세 페이지로 리다이렉트, 미인증 시 로그인 페이지로 리다이렉트
     */
    @PostMapping("/stories/{id}/comments")
    public String createComment(
            @PathVariable Long id,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/member/signin";
        }

        // 사용자 닉네임 가져오기
        Member member = memberService.findByUsername(userDetails.getUsername());
        String nickname = (member != null && member.getNickname() != null) ? member.getNickname()
                : userDetails.getUsername();

        storyCommentService.createComment(id, content, nickname, userDetails.getUsername());

        return "redirect:/stories/" + id + "?success=댓글이 등록되었습니다.";
    }

    // 스토리 댓글 삭제
    /**
     * 스토리 댓글을 삭제합니다. 작성자 본인만 삭제할 수 있습니다.
     *
     * @param commentId    댓글 식별자
     * @param storyId      소속 스토리 식별자
     * @param userDetails  인증 사용자 정보(필수)
     * @return 상세 페이지로 리다이렉트. 권한 오류 시 `?error=메시지` 쿼리 파라미터 포함
     */
    @PostMapping("/stories/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long storyId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/member/signin";
        }

        try {
            storyCommentService.deleteComment(commentId, userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            return "redirect:/stories/" + storyId + "?error=" + e.getMessage();
        }

        return "redirect:/stories/" + storyId + "?success=댓글이 삭제되었습니다.";
    }

    // 스토리 댓글 수정
    /**
     * 스토리 댓글을 수정합니다. 작성자 본인만 수정할 수 있습니다.
     *
     * @param commentId    댓글 식별자
     * @param content      변경할 내용
     * @param storyId      소속 스토리 식별자
     * @param userDetails  인증 사용자 정보(필수)
     * @return 상세 페이지로 리다이렉트. 권한 오류 시 `?error=메시지` 쿼리 파라미터 포함
     */
    @PostMapping("/stories/comments/{commentId}/update")
    public String updateComment(
            @PathVariable Long commentId,
            @RequestParam String content,
            @RequestParam Long storyId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/member/signin";
        }

        try {
            storyCommentService.updateComment(commentId, content, userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            return "redirect:/stories/" + storyId + "?error=" + e.getMessage();
        }

        return "redirect:/stories/" + storyId + "?success=댓글이 수정되었습니다.";
    }

    // 스토리 작성 페이지
    /**
     * 스토리 작성 폼을 표시합니다.
     *
     * @return 작성 폼 뷰 이름(`stories/write`)
     */
    @GetMapping("/stories/write")
    public String writeForm() {
        return "stories/write";
    }

    // 스토리 저장
    /**
     * 새 스토리를 생성합니다. 선택적으로 영상/썸네일 파일 업로드를 처리합니다.
     * <p>
     * 업로드 파일은 `uploads/videos/`, `uploads/images/` 하위에 저장되며,
     * 뷰에서 접근 가능한 URL(`/uploads/...`)을 반환합니다.
     * </p>
     *
     * @param title         제목
     * @param author        작성자 표시명
     * @param description   설명/본문 요약
     * @param category      선택적 카테고리
     * @param tags          선택적 태그(구분자는 구현에 따름)
     * @param videoFile     선택적 동영상 파일
     * @param thumbnailFile 선택적 썸네일 이미지 파일
     * @param model         에러 메시지 표시에 사용
     * @return 생성 후 목록으로 리다이렉트, 업로드 실패 시 작성 폼으로 이동
     */
    @PostMapping("/stories/write")
    public String write(
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) MultipartFile videoFile,
            @RequestParam(required = false) MultipartFile thumbnailFile,
            Model model) {
        System.out.println("=== 스토리 작성 시작 ===");
        Story story = new Story();
        story.setTitle(title);
        story.setAuthor(author);
        story.setDescription(description);
        story.setCategory(category);
        story.setTags(tags);
        story.setPublic(true);

        // 동영상 파일 업로드 처리
        if (videoFile != null && !videoFile.isEmpty()) {
            String videoUrl = saveVideoFile(videoFile);
            if (videoUrl == null || videoUrl.isBlank()) {
                model.addAttribute("error", "동영상 파일 저장에 실패했습니다. 다시 시도해 주세요.");
                model.addAttribute("title", title);
                model.addAttribute("author", author);
                model.addAttribute("description", description);
                model.addAttribute("category", category);
                model.addAttribute("tags", tags);
                return "stories/write";
            }
            story.setVideoUrl(videoUrl);
        } else {
            story.setVideoUrl("");
        }

        // 썸네일 이미지 업로드 처리
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String thumbnailUrl = saveThumbnailFile(thumbnailFile);
            if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
                model.addAttribute("error", "썸네일 이미지 저장에 실패했습니다. 다시 시도해 주세요.");
                model.addAttribute("title", title);
                model.addAttribute("author", author);
                model.addAttribute("description", description);
                model.addAttribute("category", category);
                model.addAttribute("tags", tags);
                return "stories/write";
            }
            story.setThumbnailUrl(thumbnailUrl);
        }

        storyService.saveStory(story);
        System.out.println("=== 스토리 저장 완료 ===");
        return "redirect:/stories";
    }

    // 스토리 수정 페이지
    /**
     * 스토리 수정 폼을 표시합니다.
     *
     * @param id    스토리 식별자
     * @param model 뷰 렌더링용 모델
     * @return 수정 폼 뷰 이름(`stories/edit`), 존재하지 않으면 목록으로 리다이렉트
     */
    @GetMapping("/stories/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Story story = storyService.getStoryById(id);
        if (story == null) {
            return "redirect:/stories";
        }
        model.addAttribute("story", story);
        return "stories/edit";
    }

    // 스토리 수정 처리
    /**
     * 스토리를 수정합니다. 선택적으로 영상/썸네일 교체 및 썸네일 삭제를 처리합니다.
     *
     * @param id             스토리 식별자
     * @param title          제목
     * @param author         작성자 표시명
     * @param description    설명/본문 요약
     * @param category       선택적 카테고리
     * @param tags           선택적 태그
     * @param videoFile      선택적 교체용 동영상 파일
     * @param thumbnailFile  선택적 교체용 썸네일 이미지 파일
     * @param deleteThumbnail 썸네일 삭제 여부(true 시 기존 파일 삭제)
     * @param model          에러 메시지 표시에 사용
     * @return 수정 후 상세로 리다이렉트, 실패 시 수정 폼으로 이동
     */
    @PostMapping("/stories/edit/{id}")
    public String edit(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) MultipartFile videoFile,
            @RequestParam(required = false) MultipartFile thumbnailFile,
            @RequestParam(required = false) Boolean deleteThumbnail,
            Model model) {
        Story story = storyService.getStoryById(id);
        if (story == null) {
            return "redirect:/stories";
        }

        story.setTitle(title);
        story.setAuthor(author);
        story.setDescription(description);
        story.setCategory(category);
        story.setTags(tags);

        // 새 동영상 파일이 업로드된 경우에만 교체
        if (videoFile != null && !videoFile.isEmpty()) {
            String existingUrl = story.getVideoUrl();
            deleteVideoFileByUrl(existingUrl);
            String videoUrl = saveVideoFile(videoFile);
            if (videoUrl == null || videoUrl.isBlank()) {
                model.addAttribute("error", "동영상 파일 저장에 실패했습니다. 다시 시도해 주세요.");
                model.addAttribute("story", story);
                return "stories/edit";
            }
            story.setVideoUrl(videoUrl);
        }

        // 썸네일 삭제 체크박스가 체크된 경우
        if (Boolean.TRUE.equals(deleteThumbnail)) {
            String existingThumbnail = story.getThumbnailUrl();
            deleteImageFileByUrl(existingThumbnail);
            story.setThumbnailUrl(null);
        }
        // 새 썸네일 이미지가 업로드된 경우에만 교체 (삭제 체크박스가 없을 때만)
        else if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String existingThumbnail = story.getThumbnailUrl();
            deleteImageFileByUrl(existingThumbnail);
            String thumbnailUrl = saveThumbnailFile(thumbnailFile);
            if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
                model.addAttribute("error", "썸네일 이미지 저장에 실패했습니다. 다시 시도해 주세요.");
                model.addAttribute("story", story);
                return "stories/edit";
            }
            story.setThumbnailUrl(thumbnailUrl);
        }

        storyService.saveStory(story);
        return "redirect:/stories/" + id;
    }

    // 동영상 파일 삭제 메서드 (URL 기반, uploads/videos 하위만 허용)
    /**
     * 동영상 파일을 URL 기준으로 삭제합니다. `uploads/videos` 하위에서만 동작합니다.
     *
     * @param existingUrl `/uploads/videos/...` 형식의 URL(그 외는 무시)
     */
    private void deleteVideoFileByUrl(String existingUrl) {
        if (existingUrl == null || existingUrl.isBlank())
            return;
        try {
            if (existingUrl.startsWith("/uploads/videos/")) {
                String existingName = existingUrl.substring(existingUrl.lastIndexOf('/') + 1);
                Path uploadsDir = Paths.get("uploads/videos").toAbsolutePath().normalize();
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

    // 동영상 파일 저장 메서드
    /**
     * 동영상 파일을 저장하고 접근 가능한 URL을 반환합니다.
     *
     * @param file 업로드된 동영상 파일
     * @return `/uploads/videos/...` URL, 실패 시 빈 문자열
     */
    private String saveVideoFile(MultipartFile file) {
        try {
            // 업로드 디렉토리 생성
            String uploadDir = "uploads/videos/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path filePath = Paths.get(uploadDir + uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 웹에서 접근 가능한 URL 반환
            return "/uploads/videos/" + uniqueFilename;

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // 썸네일 이미지 파일 저장 메서드
    /**
     * 썸네일 이미지 파일을 저장하고 접근 가능한 URL을 반환합니다.
     *
     * @param file 업로드된 이미지 파일
     * @return `/uploads/images/...` URL, 실패 시 빈 문자열
     */
    private String saveThumbnailFile(MultipartFile file) {
        try {
            System.out.println("saveThumbnailFile 메서드 시작");
            // 업로드 디렉토리 생성
            String uploadDir = "uploads/images/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println("디렉토리 생성: " + created);
            }
            System.out.println("업로드 디렉토리: " + dir.getAbsolutePath());

            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = "story_thumb_" + UUID.randomUUID().toString() + extension;
            System.out.println("생성된 파일명: " + uniqueFilename);

            // 파일 저장
            Path filePath = Paths.get(uploadDir + uniqueFilename);
            System.out.println("파일 저장 경로: " + filePath.toAbsolutePath());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("파일 저장 완료");

            // 웹에서 접근 가능한 URL 반환
            String resultUrl = "/uploads/images/" + uniqueFilename;
            System.out.println("반환 URL: " + resultUrl);
            return resultUrl;

        } catch (IOException e) {
            System.err.println("썸네일 저장 중 오류 발생:");
            e.printStackTrace();
            return "";
        }
    }

    // 이미지 파일 삭제 메서드 (URL 기반, uploads/images 하위만 허용)
    /**
     * 이미지 파일을 URL 기준으로 삭제합니다. `uploads/images` 하위에서만 동작합니다.
     *
     * @param existingUrl `/uploads/images/...` 형식의 URL(그 외는 무시)
     */
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

    // 스토리 삭제 (연관 동영상/썸네일 파일도 함께 삭제)
    /**
     * 스토리를 삭제합니다. 관련된 로컬 동영상/썸네일 파일도 함께 정리합니다.
     *
     * @param id 스토리 식별자
     * @return 목록으로 리다이렉트
     */
    @PostMapping("/stories/delete/{id}")
    public String delete(@PathVariable Long id) {
        Story story = storyService.getStoryById(id);
        if (story != null) {
            deleteVideoFileByUrl(story.getVideoUrl());
            deleteImageFileByUrl(story.getThumbnailUrl());
            storyService.deleteStory(id);
        }
        return "redirect:/stories";
    }
}
