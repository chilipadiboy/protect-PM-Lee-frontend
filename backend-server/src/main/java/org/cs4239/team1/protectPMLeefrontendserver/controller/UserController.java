package org.cs4239.team1.protectPMLeefrontendserver.controller;

import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.UserSummary;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.RecordService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public UserSummary getCurrentUser(@CurrentUser User currentUser) {
        return new UserSummary(currentUser.getNric(), currentUser.getName(),
                currentUser.getSelectedRole().toString(),
                currentUser.getPhone(), currentUser.getEmail());
    }
}
