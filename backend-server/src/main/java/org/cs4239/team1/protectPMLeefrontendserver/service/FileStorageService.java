package org.cs4239.team1.protectPMLeefrontendserver.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.FileNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.FileStorageException;
import org.cs4239.team1.protectPMLeefrontendserver.storage.FileStorageProperties;
import org.cs4239.team1.protectPMLeefrontendserver.storage.LogsStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final Path logsStorageLocation;
    private static final Collection<String> ALLOWED_FILE_TYPES = Arrays.asList("txt", "csv", "jpg", "png", "mp4");

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties, LogsStorageProperties logsStorageProperties) {
        fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        logsStorageLocation = Paths.get(logsStorageProperties.getFile())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(fileStorageLocation);
            new File(logsStorageLocation.toUri()).createNewFile();
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the files.", ex);
        }
    }

    @PreAuthorize("hasRole('THERAPIST')")
    public String storeFile(MultipartFile file, String nric) throws FileUploadException {
        if (!ALLOWED_FILE_TYPES.contains(FilenameUtils.getExtension(file.getOriginalFilename()))) {
            throw new FileUploadException("Invalid file type.");
        }

        String cleanedFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String distinctCleanedFileName = nric + "_" + LocalDateTime.now().toString().replaceAll(":", ".") + "_" + cleanedFileName;
        Path targetLocation = fileStorageLocation.resolve(distinctCleanedFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + distinctCleanedFileName + ". Please try again!", ex);
        }

        return distinctCleanedFileName;
    }

    @PreAuthorize("hasRole('THERAPIST') or hasRole('RESEARCHER')")
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found " + fileName, ex);
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public Resource loadLogs() {
        try {
            return new UrlResource(logsStorageLocation.toUri());
        } catch (MalformedURLException ex) {
            throw new AssertionError("Logs should exist.");
        }
    }
}