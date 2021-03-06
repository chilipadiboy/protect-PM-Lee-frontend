package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.repository.PermissionRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.sf.jmimemagic.Magic;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private static final Collection<String> ALLOWED_FILE_TYPES = Arrays.asList("txt", "csv", "jpg", "png", "mp4");

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Value("${app.privateKey}")
    private String privateKey;

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@CurrentUser User user, @PathVariable String fileName) {
        logger.info("NRIC_" + user.getNric() + " ROLE_" + user.getSelectedRole() + " accessing FileController#downloadFile", fileName);

        if (user.getSelectedRole().equals(Role.ROLE_PATIENT)) {
            Optional<Record> record = recordRepository.findByPatientIC(user.getNric()).stream()
                    .filter(r -> r.getDocument().equals(fileName))
                    .findFirst();
            if (!record.isPresent()) {
                throw new BadRequestException("Unauthorised access to this file.");
            }
        } else if (user.getSelectedRole().equals(Role.ROLE_THERAPIST)) {
            // TODO: Therapist checks through all the records he has access to.
            //look for the record
            Record record = recordRepository.findByDocument(fileName)
                    .orElseThrow(() -> new ResourceNotFoundException("File", "filename", fileName));
            //check if therapist have permission to view record
            Permission permission = permissionRepository.findByUserAndRecord(user, record)
                    .orElseThrow(() -> new BadRequestException("Unauthorised access to this file."));
        }

        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            String contentType = Magic.getMagicMatch(IOUtils.toByteArray(resource.getInputStream())).getMimeType();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new AssertionError("Should not happen.");
        }
    }
}