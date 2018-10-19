package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.security.GeneralSecurityException;

import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserAuthentication {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Returns the {@code User} with the credentials {@code nric}, {@code password}, {@code role},
     * where {@code User#selectedRole} is assigned to {@code role}.
     * Throws {@code GeneralSecurityException} otherwise.
     */
    public User authenticate(String nric, String password, Role role) throws GeneralSecurityException {
        User loadedUser = userDetailsService.loadUserByUsername(nric);

        if (loadedUser == null
                || !passwordEncoder.matches(password, loadedUser.getPassword())
                || !loadedUser.hasRole(role)) {
            throw new GeneralSecurityException();
        }

        loadedUser.setSelectedRole(role);
        return loadedUser;
    }
}
