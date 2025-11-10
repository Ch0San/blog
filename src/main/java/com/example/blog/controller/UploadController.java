package com.example.blog.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

/**
 * 업로드 컨트롤러: 에디터 내 이미지 비동기 업로드
 */
@Controller
@RequestMapping("/api/uploads")
public class UploadController {

    private static final long MAX_IMAGE_SIZE = 4L * 1024 * 1024; // 4MB
    private static final String TEMP_UPLOADS_KEY = "tempUploadedImages";

    /**
     * 에디터 이미지 1개를 업로드합니다.
     *
     * @param file    업로드할 이미지 파일(최대 4MB)
     * @param session 임시 업로드 목록을 보관할 세션
     * @return 업로드된 이미지의 접근 URL(`/uploads/images/...`)
     */
    @PostMapping(path = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, HttpSession session) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "EMPTY_FILE"));
            }
            if (file.getSize() > MAX_IMAGE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "IMAGE_TOO_LARGE"));
            }

            Path uploadRoot = Paths.get("uploads", "images");
            Files.createDirectories(uploadRoot);

            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.'))
                    : "";
            String filename = LocalDate.now() + "-" + UUID.randomUUID() + ext;
            Path target = uploadRoot.resolve(filename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            String webPath = "/uploads/images/" + filename;

            // 세션에 임시 업로드 목록 추가
            @SuppressWarnings("unchecked")
            Set<String> tempUploads = (Set<String>) session.getAttribute(TEMP_UPLOADS_KEY);
            if (tempUploads == null) {
                tempUploads = new HashSet<>();
                session.setAttribute(TEMP_UPLOADS_KEY, tempUploads);
            }
            tempUploads.add(webPath);

            Map<String, Object> result = new HashMap<>();
            result.put("url", webPath);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "UPLOAD_FAIL"));
        }
    }

    /**
     * 임시 업로드된 이미지 정리: 실제 사용된 이미지 제외하고 삭제
     */
    /**
     * 임시 업로드된 이미지 정리: 본문에서 사용되지 않은 항목을 삭제합니다.
     *
     * @param usedImagesParam 콤마로 구분된 사용된 이미지 URL 목록(선택)
     * @param session         세션에 보관된 임시 업로드 목록
     * @return 삭제 개수 또는 상태 메시지
     */
    @PostMapping("/cleanup")
    @ResponseBody
    public ResponseEntity<?> cleanupTempUploads(
            @RequestParam(value = "usedImages", required = false) String usedImagesParam,
            HttpSession session) {
        try {
            @SuppressWarnings("unchecked")
            Set<String> tempUploads = (Set<String>) session.getAttribute(TEMP_UPLOADS_KEY);
            if (tempUploads == null || tempUploads.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "NO_TEMP_FILES"));
            }

            // 실제 사용된 이미지 목록 파싱
            Set<String> usedImages = new HashSet<>();
            if (usedImagesParam != null && !usedImagesParam.trim().isEmpty()) {
                String[] urls = usedImagesParam.split(",");
                for (String url : urls) {
                    String trimmed = url.trim();
                    if (!trimmed.isEmpty()) {
                        usedImages.add(trimmed);
                    }
                }
            }

            // 사용되지 않은 이미지 삭제
            int deleted = 0;
            for (String tempUrl : tempUploads) {
                if (!usedImages.contains(tempUrl)) {
                    try {
                        // /uploads/images/xxx.jpg -> uploads/images/xxx.jpg
                        String relativePath = tempUrl.startsWith("/") ? tempUrl.substring(1) : tempUrl;
                        Path filePath = Paths.get(relativePath);
                        if (Files.exists(filePath)) {
                            Files.delete(filePath);
                            deleted++;
                        }
                    } catch (Exception e) {
                        // 개별 파일 삭제 실패는 무시
                    }
                }
            }

            // 세션에서 임시 목록 제거
            session.removeAttribute(TEMP_UPLOADS_KEY);

            return ResponseEntity.ok(Map.of("deleted", deleted));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "CLEANUP_FAIL"));
        }
    }
}
