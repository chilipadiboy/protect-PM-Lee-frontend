package org.cs4239.team1.protectPMLeefrontendserver.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.persistence.ElementCollection;

import org.cs4239.team1.protectPMLeefrontendserver.model.audit.DateAudit;
import org.hibernate.annotations.NaturalId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
            "nric"
        }),
        @UniqueConstraint(columnNames = {
            "email"
        })
})
public class User extends DateAudit implements UserDetails {
    @Id
    @NonNull
    @NotBlank
    @Size(min = 9, max = 9)
    private String nric;

    @NonNull
    @NotBlank
    @Size(max = 40)
    private String name;

    @NaturalId
    @NonNull
    @NotBlank
    @Size(max = 40)
    @Email
    private String email;

    @NonNull
    @NotBlank
    @Size(max = 20)
    private String phone;

    @NonNull
    @NotBlank
    @Size(max = 100)
    private String address;

    @NonNull
    private int age;

    @NonNull
    private Gender gender;

    @NonNull
    @NotBlank
    @Size(max = 100)
    private String password;

    @Getter
    @NonNull
    @ElementCollection(fetch = FetchType.EAGER, targetClass = Role.class)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @Transient
    private Role selectedRole;

    public void setSelectedRole(Role selectedRole) {
        if (this.selectedRole != null) {
            throw new AssertionError("This method should not be called when selectedAuthority already has a value");
        }

        if (!roles.contains(selectedRole)) {
            throw new IllegalArgumentException();
        }

        this.selectedRole = selectedRole;
    }

    public boolean hasRole(Role selectedRole) {
        return roles.contains(selectedRole);
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
        return Collections.singletonList(new SimpleGrantedAuthority(selectedRole.name()));
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