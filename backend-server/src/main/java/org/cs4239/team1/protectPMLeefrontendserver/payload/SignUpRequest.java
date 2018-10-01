package org.cs4239.team1.protectPMLeefrontendserver.payload;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;

@Getter
public class SignUpRequest {
    @NotBlank
    @Size(min = 9, max = 9)
    private String nric;

    @NotBlank
    @Size(max = 40)
    private String name;

    @NotBlank
    @Size(max = 40)
    @Email
    private String email;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(max = 100)
    private String address;

    private int age;

    private String gender;

    @NotBlank
    @Size(max = 100)
    private String password;

    @NotNull
    @Size(min = 1)
    private List<String> roles;
}
