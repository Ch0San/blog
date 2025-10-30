package com.example.blog.controller;

import com.example.blog.service.SiteSettingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/site")
public class AdminSiteController {

    private final SiteSettingService siteSettingService;

    public AdminSiteController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping("/hero-image")
    public String heroImageEditPage(Model model) {
        String currentUrl = siteSettingService.getSetting("site_hero_image_url", "/images/index_image.jpg");
        model.addAttribute("heroImageUrl", currentUrl);

        // 과거 hero 이미지 목록 수집 (uploads/images/hero_*.{ext})
        try {
            Path uploadDir = Paths.get("uploads", "images");
            Files.createDirectories(uploadDir);
            var files = Files.list(uploadDir)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.startsWith("hero_") && (name.endsWith(".jpg") || name.endsWith(".jpeg")
                                || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".webp"));
                    })
                    .sorted((a, b) -> Long.compare(b.toFile().lastModified(), a.toFile().lastModified()))
                    .map(p -> "/uploads/images/" + p.getFileName())
                    .toList();
            model.addAttribute("heroImageList", files);
        } catch (IOException ignored) {
            model.addAttribute("heroImageList", java.util.List.of());
        }

        return "mainImageEdit";
    }

    @PostMapping("/hero-image")
    public String uploadHeroImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "redirect:/admin/site/hero-image?error=empty";
        }

        // 간단한 이미지 타입 검증
        String contentType = file.getContentType();
        if (contentType == null) {
            return "redirect:/admin/site/hero-image?error=type";
        }
        Set<String> allowed = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
        if (!allowed.contains(contentType.toLowerCase())) {
            return "redirect:/admin/site/hero-image?error=type";
        }

        // 업로드 폴더: uploads/images
        Path uploadDir = Paths.get("uploads", "images");
        Files.createDirectories(uploadDir);

        // 파일 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null) {
            String filename = StringUtils.getFilename(originalFilename);
            String extension = StringUtils.getFilenameExtension(filename);
            if (extension != null) {
                ext = "." + extension.toLowerCase();
            }
        }
        if (ext.isEmpty()) {
            // contentType 기준 확장자 기본값
            if ("image/png".equals(contentType))
                ext = ".png";
            else if ("image/webp".equals(contentType))
                ext = ".webp";
            else if ("image/gif".equals(contentType))
                ext = ".gif";
            else
                ext = ".jpg";
        }

        // 파일명: hero_<timestamp>.<ext>
        String filename = "hero_" + System.currentTimeMillis() + ext;
        Path target = uploadDir.resolve(filename);
        file.transferTo(target);

        // 설정에 저장되는 공개 URL
        String publicUrl = "/uploads/images/" + filename;
        siteSettingService.saveSetting("site_hero_image_url", publicUrl, "Index hero image URL");

        return "redirect:/admin/site/hero-image?updated=1";
    }

    @PostMapping("/hero-image/delete")
    public String deleteHeroImage(@RequestParam("url") String url) {
        if (url == null || url.isBlank()) {
            return "redirect:/admin/site/hero-image?error=badurl";
        }
        String trimmed = url.trim();
        // 업로드 경로 및 파일명 제한(보호): /uploads/images/hero_ 로 시작하는 파일만 허용
        if (!(trimmed.startsWith("/uploads/images/hero_") || trimmed.startsWith("uploads/images/hero_"))) {
            return "redirect:/admin/site/hero-image?error=forbidden";
        }
        try {
            Path uploadsRoot = Paths.get("uploads").toAbsolutePath().normalize();
            String relative = trimmed.startsWith("/") ? trimmed.substring(1) : trimmed; // remove leading '/'
            Path target = uploadsRoot.resolve(relative.substring("uploads/".length())).normalize();
            if (!target.startsWith(uploadsRoot)) {
                return "redirect:/admin/site/hero-image?error=forbidden";
            }
            // 현재 사용중 이미지면 기본값으로 되돌림
            String currentUrl = siteSettingService.getSetting("site_hero_image_url", "/images/index_image.jpg");
            if (trimmed.equals(currentUrl)) {
                siteSettingService.saveSetting("site_hero_image_url", "/images/index_image.jpg",
                        "Index hero image URL");
            }
            Files.deleteIfExists(target);
            return "redirect:/admin/site/hero-image?updated=1";
        } catch (Exception ex) {
            return "redirect:/admin/site/hero-image?error=delete";
        }
    }
}
