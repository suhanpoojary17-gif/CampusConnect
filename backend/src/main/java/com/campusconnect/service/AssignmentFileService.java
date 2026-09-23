package com.campusconnect.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class AssignmentFileService {

    private final Path uploadDirectory =
            Paths.get("uploads/assignments");

    public String saveFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Files.createDirectories(uploadDirectory);

            String originalFilename = file.getOriginalFilename();

            String extension = "";

            if (originalFilename != null &&
                    originalFilename.contains(".")) {

                extension = originalFilename.substring(
                        originalFilename.lastIndexOf(".")
                );
            }

            String filename =
                    "assignment_" + UUID.randomUUID() + extension;

            Path filePath = uploadDirectory.resolve(filename);

            Files.copy(
                    file.getInputStream(),
                    filePath
            );

            return filePath.toString();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to save assignment attachment",
                    e
            );
        }
    }

    public Resource loadFile(String attachmentPath) {

        try {

            Path filePath = Paths.get(attachmentPath);

            Resource resource =
                    new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException(
                        "Assignment attachment not found"
                );
            }

            return resource;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load assignment attachment",
                    e
            );
        }
    }

    public void deleteFile(String attachmentPath) {

        if (attachmentPath == null ||
                attachmentPath.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(
                    Paths.get(attachmentPath)
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to delete assignment attachment",
                    e
            );
        }
    }
}