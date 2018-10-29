package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.util.stream.Stream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.UserSummary;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/me")
    public UserSummary getCurrentUser(@CurrentUser User currentUser) {
        return new UserSummary(currentUser.getNric(), currentUser.getName(),
                currentUser.getSelectedRole().toString(),
                currentUser.getPhone(), currentUser.getEmail());
    }

    @GetMapping("/logout")
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
}
