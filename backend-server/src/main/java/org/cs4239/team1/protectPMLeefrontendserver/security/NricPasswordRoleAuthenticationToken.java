package org.cs4239.team1.protectPMLeefrontendserver.security;

import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import lombok.Getter;

@Getter
public class NricPasswordRoleAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private Role role;

    public NricPasswordRoleAuthenticationToken(Object principal, Object credentials, Role role) {
        super(principal, credentials);
        this.role = role;
    }
}
