package com.shrinetours.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    public String uploadAvatar(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename() == null ? "avatar.jpg" : file.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalName;

        Path path = Paths.get(uploadDir, "avatars", fileName).toAbsolutePath().normalize();

        Files.createDirectories(path.getParent());
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return baseUrl + "/uploads/avatars/" + fileName;
    }
}