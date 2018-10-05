package org.cs4239.team1.protectPMLeefrontendserver.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import lombok.Getter;

@Getter
public class NricPasswordRoleAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private Object role;

    public NricPasswordRoleAuthenticationToken(Object principal, Object credentials, Object role) {
        super(principal, credentials);
        this.role = role;
    }
}
