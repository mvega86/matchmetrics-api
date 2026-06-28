package com.matchmetrics.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final long MAX_SIZE = 5 * 1024 * 1024L; // 5 MB

    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    record UploadResponse(String url) {}

    @PostMapping
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("No file provided");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File exceeds 5 MB limit");
        }

        try {
            byte[] header = readHeader(file.getInputStream(), 12);
            if (!isValidImageBytes(header)) {
                throw new IllegalArgumentException("File content does not match an allowed image type");
            }

            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            String originalFilename = file.getOriginalFilename();
            String ext = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                    : ".jpg";
            String filename = UUID.randomUUID() + ext;

            Path dest = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), dest);

            String url = baseUrl + "/uploads/" + filename;
            log.info("File uploaded: {}", url);
            return ResponseEntity.ok(new UploadResponse(url));

        } catch (IOException e) {
            log.error("Upload failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", e);
        }
    }

    private byte[] readHeader(InputStream in, int bytes) throws IOException {
        try (in) {
            return in.readNBytes(bytes);
        }
    }

    private boolean isValidImageBytes(byte[] h) {
        if (h.length < 3) return false;
        // JPEG: FF D8 FF
        if (h[0] == (byte) 0xFF && h[1] == (byte) 0xD8 && h[2] == (byte) 0xFF) return true;
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (h.length >= 8 && h[0] == (byte) 0x89 && h[1] == 0x50 && h[2] == 0x4E && h[3] == 0x47) return true;
        // GIF87a or GIF89a: GIF8
        if (h[0] == 0x47 && h[1] == 0x49 && h[2] == 0x46 && h[3] == 0x38) return true;
        // WEBP: RIFF....WEBP
        if (h.length >= 12 && h[0] == 0x52 && h[1] == 0x49 && h[2] == 0x46 && h[3] == 0x46
                && h[8] == 0x57 && h[9] == 0x45 && h[10] == 0x42 && h[11] == 0x50) return true;
        return false;
    }
}
