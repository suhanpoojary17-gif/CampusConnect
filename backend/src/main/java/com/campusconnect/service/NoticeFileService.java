package com.campusconnect.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class NoticeFileService {

    private final Path uploadDirectory =
            Paths.get("uploads/notices");

    public String saveFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        try {

            Files.createDirectories(uploadDirectory);

            String originalFileName = file.getOriginalFilename();

            String fileExtension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension =
                        originalFileName.substring(
                                originalFileName.lastIndexOf(".")
                        );
            }

            String fileName =
                    "notice_" + UUID.randomUUID() + fileExtension;

            Path filePath =
                    uploadDirectory.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return filePath.toString();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to save notice attachment",
                    e
            );
        }
    }

    public Resource loadFile(String filePath) {

        try {

            Path path = Paths.get(filePath);

            Resource resource = new UrlResource(
                    path.toUri()
            );

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException(
                        "Attachment not found"
                );
            }

            return resource;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load notice attachment",
                    e
            );
        }
    }
}