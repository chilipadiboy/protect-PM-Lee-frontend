package org.cs4239.team1.protectPMLeefrontendserver.payload;

import javax.validation.constraints.NotBlank;

import lombok.Getter;

@Getter
public class LoginRequest {
    @NotBlank
    private String nric;

    @NotBlank
    private String password;

    @NotBlank
    private String role;
}
