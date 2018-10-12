package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.cs4239.team1.protectPMLeefrontendserver.payload.UserSummary;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/showAllUsers")
    public List<UserSummary> showAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserSummary(user.getNric(), user.getName(),
                        user.getRoles().toString().toLowerCase(), user.getPhone(), user.getEmail()))
                .collect(Collectors.toList());
    }
}
