package org.cs4239.team1.protectPMLeefrontendserver.security;

import lombok.AllArgsConstructor;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.GeneralSecurityException;
import java.util.Collections;

@AllArgsConstructor
public class UserAuthenticationProvider implements AuthenticationProvider {
    private UserAuthentication userAuthentication;

    @Override
    public Authentication authenticate(Authentication authentication) {
        UserAuthenticationToken authToken = (UserAuthenticationToken) authentication;
        User loadedUser = authenticate(authToken);

        return new UsernamePasswordAuthenticationToken(loadedUser, loadedUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(loadedUser.getSelectedRole().name())));
    }

    @Override
    public boolean supports(Class<?> auth) {
        return auth.equals(UserAuthenticationToken.class);
    }

    /**
     * Returns the {@code User} with the credentials of {@code authToken} if such a user exists.
     * Throws {@code BadCredentialsException} otherwise.
     */
    private User authenticate(UserAuthenticationToken authToken) {
        String presentedNric = authToken.getName();
        String presentedPassword = authToken.getCredentials().toString();
        Role presentedRole = authToken.getRole();

        try {
            User loadedUser = userAuthentication.authenticate(presentedNric, presentedPassword, presentedRole);
            //Ed25519Verify verifier = new Ed25519Verify(Base64.getDecoder().decode(loadedUser.getPublicKey()));
            //verifier.verify(authToken.getSignature(), authToken.getIv());

            return loadedUser;
        } catch (GeneralSecurityException gse) {
            throw new BadCredentialsException("Bad credentials.");
        }
    }
}
