package com.example.blog.controller;

import com.example.blog.repository.PostRepository;
import com.example.blog.repository.PostImageRepository;
import com.example.blog.repository.StoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 운영 전 점검용 간단한 디버그 API
 * 관리자(ROLE_ADMIN)만 접근 가능하도록 SecurityConfig의 경로 규칙(/member/admin/**)에 맞춘다.
 */
@RestController
public class AdminDebugController {
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final StoryRepository storyRepository;

    public AdminDebugController(PostRepository postRepository,
            PostImageRepository postImageRepository,
            StoryRepository storyRepository) {
        this.postRepository = postRepository;
        this.postImageRepository = postImageRepository;
        this.storyRepository = storyRepository;
    }

    @GetMapping("/member/admin/media-urls")
    public Map<String, Object> mediaUrls() {
        Map<String, Object> result = new HashMap<>();

        var postPage = postRepository.findAll(PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "id")));
        List<Map<String, Object>> posts = postPage.getContent().stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("title", p.getTitle());
            m.put("thumbnailUrl", p.getThumbnailUrl());
            m.put("videoUrl", p.getVideoUrl());
            m.put("thumbnailOk", isOk(p.getThumbnailUrl()));
            m.put("videoOk", isOk(p.getVideoUrl()));
            return m;
        }).collect(Collectors.toList());
        result.put("posts", posts);

        var imgPage = postImageRepository.findAll(PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "id")));
        List<Map<String, Object>> images = imgPage.getContent().stream().map(pi -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", pi.getId());
            m.put("postId", pi.getPost() != null ? pi.getPost().getId() : null);
            m.put("imageUrl", pi.getImageUrl());
            m.put("ok", isOk(pi.getImageUrl()));
            return m;
        }).collect(Collectors.toList());
        result.put("postImages", images);

        var storyPage = storyRepository.findAll(PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "id")));
        List<Map<String, Object>> stories = storyPage.getContent().stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("title", s.getTitle());
            m.put("thumbnailUrl", s.getThumbnailUrl());
            m.put("videoUrl", s.getVideoUrl());
            m.put("thumbnailOk", isOk(s.getThumbnailUrl()));
            m.put("videoOk", isOk(s.getVideoUrl()));
            return m;
        }).collect(Collectors.toList());
        result.put("stories", stories);

        return result;
    }

    private boolean isOk(String url) {
        if (url == null || url.isBlank())
            return false;
        String u = url.trim();
        if (u.startsWith("http://") || u.startsWith("https://"))
            return true;
        return u.startsWith("/uploads/");
    }
}
