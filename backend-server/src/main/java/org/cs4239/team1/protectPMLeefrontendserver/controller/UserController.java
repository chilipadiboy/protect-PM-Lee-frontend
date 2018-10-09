package org.cs4239.team1.protectPMLeefrontendserver.controller;

import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.model.UserProfile;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.UserSummary;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.security.UserPrincipal;
import org.cs4239.team1.protectPMLeefrontendserver.service.RecordService;
import org.cs4239.team1.protectPMLeefrontendserver.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private RecordService recordService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/user/me")
    public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        return new UserSummary(currentUser.getNric(), currentUser.getName(),
                currentUser.getAuthority().toString().toLowerCase());
    }

    @GetMapping("/users/{nric}")
    public UserProfile getUserProfile(@PathVariable(value = "nric") String nric) {
        User user = userRepository.findByNric(nric)
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", nric));

        UserProfile userProfile = new UserProfile(user.getNric(), user.getName(), user.getPhone());

        return userProfile;
    }

    @GetMapping("/users/{nric}/records")
    public PagedResponse<RecordResponse> getRecordsCreatedBy(@PathVariable(value = "nric") String nric,
                                                             @CurrentUser UserPrincipal currentUser,
                                                             @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                             @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getRecordsCreatedBy(nric, currentUser, page, size);
    }
}
