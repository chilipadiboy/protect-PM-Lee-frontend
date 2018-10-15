package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.util.Collections;

import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NricPasswordRoleAuthenticationProvider implements AuthenticationProvider {
    private PasswordEncoder passwordEncoder;
    private CustomUserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        NricPasswordRoleAuthenticationToken auth = (NricPasswordRoleAuthenticationToken) authentication;

        String presentedNric = auth.getName();
        String presentedPassword = auth.getCredentials().toString();
        Role presentedRole = auth.getRole();

        User loadedUser = userDetailsService.loadUserByUsername(presentedNric);

        if (!passwordEncoder.matches(presentedPassword, loadedUser.getPassword())
                || !loadedUser.hasRole(presentedRole)) {
            throw new BadCredentialsException("Bad credentials.");
        }

        loadedUser.setSelectedRole(presentedRole);
        return new UsernamePasswordAuthenticationToken(loadedUser, presentedPassword,
                Collections.singletonList(new SimpleGrantedAuthority(presentedRole.name())));
    }

    @Override
    public boolean supports(Class<?> auth) {
        return auth.equals(NricPasswordRoleAuthenticationToken.class);
    }
}
