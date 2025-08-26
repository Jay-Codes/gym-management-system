package com.jerrycode.gym_services.business.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}") // e.g., "/var/uploads" or "C:/uploads"
    private String storagePath;

    @Value("${file.app-dir}") // e.g., "gym-services/logos" or "vipgym-services/logos"
    private String appDir;

    @Value("${file.url-prefix:/uploads}") // e.g., "/uploads" (configurable, defaults to /uploads)
    private String urlPrefix;

    public String storeLogo(MultipartFile file) throws IOException {
        logger.info("Processing file to be stored: {}", file.getOriginalFilename());
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Logo file cannot be null or empty");
        }

        // Create the full upload directory path
        Path uploadDir = Paths.get(storagePath, appDir).normalize();

        logger.debug("Preparing to store logo in: {}", uploadDir);

        // Create directory if it doesn't exist
        if (!Files.exists(uploadDir)) {
            logger.info("Creating application directory at: {}", uploadDir);
            Files.createDirectories(uploadDir);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
        String filename = "logo_" + UUID.randomUUID() + fileExtension;

        Path filePath = uploadDir.resolve(filename).normalize();
        logger.debug("Generated file path: {}", filePath);

        // Save file
        Files.copy(file.getInputStream(), filePath);
        logger.info("Successfully stored logo at: {}", filePath);

        // Return just the filename (not including appDir in the path)
        return filename;
    }

    public boolean deleteLogo(String filename) throws IOException {
        logger.info("Processing file to delete: {}", filename);
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        Path filePath = Paths.get(storagePath, appDir, filename).normalize();
        logger.debug("Attempting to delete logo at: {}", filePath);

        boolean deleted = Files.deleteIfExists(filePath);
        if (deleted) {
            logger.info("Successfully deleted logo: {}", filename);
        } else {
            logger.warn("Logo file not found for deletion: {}", filename);
        }
        return deleted;
    }

    public String getLogoUrl(String filename) {
        logger.info("Generating URL for file: {}", filename);
        if (filename == null || filename.isEmpty()) {
            return null;
        }

        // Construct URL path using the configurable prefix and application directory
        String normalizedFilename = filename.replace("\\", "/");
        String urlPath = String.format("%s/%s/%s", urlPrefix, appDir, normalizedFilename)
                .replaceAll("/+", "/"); // Remove duplicate slashes
        logger.debug("Generated logo URL: {}", urlPath);

        return urlPath;
    }

    public String getLogoBase64(String filename) throws IOException {
        logger.info("Generating Base64 for file: {}", filename);
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        Path filePath = Paths.get(storagePath, appDir, filename).normalize();
        logger.debug("Reading logo file for Base64 conversion: {}", filePath);

        if (!Files.exists(filePath)) {
            logger.warn("Logo file not found for Base64 conversion: {}", filePath);
            return "";
        }

        byte[] imageBytes = Files.readAllBytes(filePath);
        String base64String = Base64.getEncoder().encodeToString(imageBytes);
        logger.debug("Successfully converted logo to Base64, length: {}", base64String.length());

        return base64String;
    }

    // Helper method to get the full storage path (for debugging)
    public String getFullStoragePath() {
        return Paths.get(storagePath, appDir).toString();
    }
}