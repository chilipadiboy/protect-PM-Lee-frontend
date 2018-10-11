package org.cs4239.team1.protectPMLeefrontendserver.exception;

import org.springframework.security.core.AuthenticationException;

public class RoleNotFoundException extends AuthenticationException {
    public RoleNotFoundException(String msg) {
        super(msg);
    }
}
