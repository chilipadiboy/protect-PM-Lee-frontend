package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
public class LoginRequest {
    @NotBlank
    @Size(min = 9, max = 9)
    private String nric;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    @Pattern(regexp = "patient|therapist|administrator|researcher|external partner")
    private String role;

    @NotBlank
    private String encryptedString;

    @NotBlank
    private String iv;
}
