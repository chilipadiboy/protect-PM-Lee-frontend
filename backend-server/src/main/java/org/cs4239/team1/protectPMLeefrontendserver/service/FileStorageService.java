package org.cs4239.team1.protectPMLeefrontendserver.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.cs4239.team1.protectPMLeefrontendserver.exception.FileNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.FileStorageException;
import org.cs4239.team1.protectPMLeefrontendserver.storage.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, String nric) {
        String cleanedFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String distinctCleanedFileName = nric + "_" + LocalDateTime.now() + "_" + cleanedFileName;
        Path targetLocation = fileStorageLocation.resolve(distinctCleanedFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + distinctCleanedFileName + ". Please try again!", ex);
        }

        return distinctCleanedFileName;
    }

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

    /**
     * Assumes that there are no folders in {@code fileStorageLocation}.
     */
    public List<String> loadAllFilePaths() {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.png");

        try {
            return Files.walk(fileStorageLocation)
                    .map(Path::getFileName)
                    .filter(matcher::matches)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException ioe) {
            throw new FileStorageException("Error in loading files");
        }
    }
}