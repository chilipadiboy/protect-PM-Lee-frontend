package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.util.stream.Stream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.UserSummary;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.RecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/user/logout")
    public ResponseEntity logOutCurrentUser(HttpServletRequest req, HttpServletResponse res) {
        Cookie[] cookies = req.getCookies();
        Cookie cookieFound = Stream.of(cookies)
                .filter(cookie -> cookie.getName().equals("testCookie"))
                .findFirst()
                .orElse(null);
        cookieFound.setPath("/api");
        cookieFound.setHttpOnly(true);
        cookieFound.setValue("1");
        res.addCookie(cookieFound);  //send overwritten cookie
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
