package org.cs4239.team1.protectPMLeefrontendserver.security;

import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import lombok.Getter;

@Getter
//TODO: Dangerous as some super methods aren't overwritten.
public class NricPasswordRoleAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private Role role;
    private byte[] signature;
    private byte[] data;

    public NricPasswordRoleAuthenticationToken(Object principal, Object credentials, Role role,
            byte[] signature, byte[] data) {
        super(principal, credentials);
        this.role = role;
        this.signature = signature;
        this.data = data;
    }
}
