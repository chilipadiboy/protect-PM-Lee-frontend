package org.cs4239.team1.protectPMLeefrontendserver.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_PATIENT,
    ROLE_THERAPIST,
    ROLE_ADMINISTRATOR,
    ROLE_RESEARCHER,
    ROLE_EXTERNAL_PARTNER;

    @Override
    public String getAuthority() {
        return name();
    }

    @Override
    public String toString() {
        return this.name().substring(5).toLowerCase().replace("_", " ");
    }

    public static Role create(String role) {
        return Role.valueOf("ROLE_" + role.toUpperCase().replace(" ", "_"));
    }
}
