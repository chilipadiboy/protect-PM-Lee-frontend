package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.util.Collection;
import java.util.Collections;

import org.cs4239.team1.protectPMLeefrontendserver.exception.RoleNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    private String nric;
    private String name;

    @JsonIgnore
    private String email;
    @JsonIgnore
    private String password;

    private GrantedAuthority authority;

    public static UserPrincipal create(User user, String role) {
        if (!user.getRoles().contains(Role.valueOf(role))) {
            throw new RoleNotFoundException("User with NRIC " + user + " does not have role: " + role);
        }

        return new UserPrincipal(
                user.getNric(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                new SimpleGrantedAuthority(role)
        );
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUsername() {
        return nric;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(authority);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
