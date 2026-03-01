package com.zjut.backend.controller;

import com.zjut.backend.common.Result;
import com.zjut.backend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private static final String UPLOAD_DIR = "uploads/plans";

    @Autowired
    private SecurityUtils securityUtils;

    @PostMapping("/plans")
    public Result uploadPlan(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        Long userId = securityUtils.getCurrentUserId();
        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String suffix = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            suffix = originalName.substring(dot);
        }

        String safeName = "plan_" + userId + "_" + UUID.randomUUID() + suffix;
        Path dir = Paths.get(UPLOAD_DIR);
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(safeName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return Result.error("文件保存失败: " + e.getMessage());
        }

        Map<String, String> data = new HashMap<>();
        data.put("url", "/api/files/plans/" + safeName);
        data.put("name", originalName);
        return Result.success(data);
    }

    @GetMapping("/plans/{filename}")
    public ResponseEntity<byte[]> downloadPlan(@PathVariable String filename) throws IOException {
        if (filename == null || filename.contains("..")) {
            return ResponseEntity.badRequest().build();
        }
        Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        byte[] data = Files.readAllBytes(filePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
