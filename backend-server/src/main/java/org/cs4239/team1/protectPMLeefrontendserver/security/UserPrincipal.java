package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {
    @NonNull
    @Getter
    private String nric;

    @NonNull
    @Getter
    private String name;

    @JsonIgnore
    @NonNull
    @Getter
    private String email;

    @JsonIgnore
    @NonNull
    @Getter
    private String password;

    @NonNull
    private Collection<GrantedAuthority> authorities;

    @Getter
    private GrantedAuthority selectedAuthority;

    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getNric(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(Role::toString)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );
    }

    public void setSelectedAuthority(GrantedAuthority selectedAuthority) {
        if (this.selectedAuthority != null) {
            throw new AssertionError("This method should not be called when selectedAuthority already has a value");
        }

        if (!authorities.contains(selectedAuthority)) {
            throw new IllegalArgumentException();
        }

        this.selectedAuthority = selectedAuthority;
    }

    public boolean hasAuthority(GrantedAuthority selectedAuthority) {
        return authorities.contains(selectedAuthority);
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
        return Collections.singletonList(selectedAuthority);
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
