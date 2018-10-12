package org.cs4239.team1.protectPMLeefrontendserver.security;

import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User loadUserByUsername(String nric) {
        return userRepository.findByNric(nric)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with nric : " + nric)
                );
    }
}