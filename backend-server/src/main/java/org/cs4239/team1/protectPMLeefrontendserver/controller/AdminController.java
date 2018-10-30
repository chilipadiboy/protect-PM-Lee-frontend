package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.UserSummary;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.security.CustomUserDetailsService;
import org.cs4239.team1.protectPMLeefrontendserver.service.FileStorageService;
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

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${logging.file}")
    private String logPath;

    @GetMapping("/showAllUsers")
    public List<UserSummary> showAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserSummary(user.getNric(), user.getName(),
                        user.getRoles().toString().toLowerCase(), user.getPhone(), user.getEmail()))
                .collect(Collectors.toList());
    }

    @GetMapping("/delete/{nric}")
    public ResponseEntity<?> deleteUser(@PathVariable(value = "nric") String nric, @CurrentUser User admin) {
        if (admin.getNric().equalsIgnoreCase(nric)) {
            throw new BadRequestException("You are not allowed to delete your own account.");
        }

        customUserDetailsService.deleteUserByUsername(nric);
        return ResponseEntity.ok(new ApiResponse(true, nric + " has been successfully deleted!"));
    }

    @GetMapping("/logs")
    public ResponseEntity<Resource> getLogs() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "logs.log" + "\"")
                .body(fileStorageService.loadLogs());
    }
}
