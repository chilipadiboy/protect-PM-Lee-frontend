package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;

@Getter
public class NricPasswordRoleAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private Object role;

    public NricPasswordRoleAuthenticationToken(Object principal, Object credentials, Object role) {
        super(principal, credentials);
        this.role = role;
    }

    public NricPasswordRoleAuthenticationToken(Object principal, Object credentials, Object role,
            Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.role = role;
    }
}
