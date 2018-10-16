package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.security.GeneralSecurityException;
import java.util.Collections;

import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NricPasswordRoleAuthenticationProvider implements AuthenticationProvider {
    private UserAuthentication userAuthentication;

    @Override
    public Authentication authenticate(Authentication authentication) {
        NricPasswordRoleAuthenticationToken authToken = (NricPasswordRoleAuthenticationToken) authentication;

        try {
            User loadedUser = userAuthentication.authenticate(authToken.getName(),
                    authToken.getCredentials().toString(),
                    authToken.getRole());

            return new UsernamePasswordAuthenticationToken(loadedUser, loadedUser.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority(loadedUser.getSelectedRole().name())));
        } catch (GeneralSecurityException gse) {
            throw new BadCredentialsException("Bad credentials.");
        }
    }

    @Override
    public boolean supports(Class<?> auth) {
        return auth.equals(NricPasswordRoleAuthenticationToken.class);
    }
}
