package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.util.stream.Stream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.model.UserProfile;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.UserSummary;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.RecordService;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

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

    @GetMapping("/user/logout")
    public ApiResponse logoutCurrentUser(HttpServletRequest req, HttpServletResponse res) {
        Cookie cookie = Stream.of(req.getCookies())
                .filter(c -> c.getName().equals("testCookie"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Logged in users should have cookies."));

        cookie.setPath("/api");
        cookie.setHttpOnly(true);
        cookie.setValue(null);
        res.addCookie(cookie);

        return new ApiResponse(true, "Successfully logged out!");
    }

    @GetMapping("/users/{nric}")
    public UserProfile getUserProfile(@PathVariable(value = "nric") String nric) {
        User user = userRepository.findByNric(nric)
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", nric));

        UserProfile userProfile = new UserProfile(user.getNric(), user.getName(), user.getPhone());

        return userProfile;
    }
}
