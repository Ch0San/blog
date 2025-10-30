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

        return "redirect:/stories/" + id;
    }

    // 스토리 댓글 삭제
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

        return "redirect:/stories/" + storyId;
    }

    // 스토리 댓글 수정
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

        return "redirect:/stories/" + storyId;
    }

    // 스토리 작성 페이지
    @GetMapping("/stories/write")
    public String writeForm() {
        return "stories/write";
    }

    // 스토리 저장
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
