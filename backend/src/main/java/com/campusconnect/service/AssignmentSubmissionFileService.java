package com.campusconnect.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AssignmentSubmissionFileService {

    private final Path uploadDir =
            Paths.get("uploads/submissions");

    public AssignmentSubmissionFileService() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create submission upload directory",
                    e
            );
        }
    }

    public String saveFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Submission file is required");
        }

        String originalFilename =
                StringUtils.cleanPath(file.getOriginalFilename());

        if (originalFilename.isBlank()) {
            throw new RuntimeException("Invalid file name");
        }

        String fileName =
                UUID.randomUUID() + "_" + originalFilename;

        Path targetLocation =
                uploadDir.resolve(fileName);

        try {
            Files.copy(
                    file.getInputStream(),
                    targetLocation,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return targetLocation.toString();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not save submission file",
                    e
            );
        }
    }

    public byte[] getFile(String filePath) {

        if (filePath == null || filePath.isBlank()) {
            throw new RuntimeException("No submission attachment found");
        }

        try {
            return Files.readAllBytes(Paths.get(filePath));

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not read submission file",
                    e
            );
        }
    }

    public void deleteFile(String filePath) {

        if (filePath == null || filePath.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not delete submission file",
                    e
            );
        }
    }
}