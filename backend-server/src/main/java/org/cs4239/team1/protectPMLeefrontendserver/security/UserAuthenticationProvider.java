package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.google.crypto.tink.subtle.Ed25519Verify;

import lombok.AllArgsConstructor;

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

            Ed25519Verify verifier = new Ed25519Verify(Base64.getDecoder().decode(loadedUser.getPublicKey()));
            verifier.verify(authToken.getSignature(), authToken.getMsgHash());

            byte[] verifyHash = Hasher.hash(NonceGenerator.getNonce(presentedNric));
            if (!Arrays.equals(authToken.getMsgHash(), verifyHash)) {
                throw new GeneralSecurityException();
            }

            return loadedUser;
        } catch (Exception e) {
            throw new BadCredentialsException("Bad credentials.");
        }
    }
}
