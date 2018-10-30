package org.cs4239.team1.protectPMLeefrontendserver.payload;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;

@Getter
public class SignUpRequest {
	@NotBlank
    @Size(min = 9, max = 9)
    private String nric;

	@NotBlank
    @Size(min = 1, max = 40)
    private String name;

    @NotBlank
    @Size(min = 1, max = 40)
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 15)
    private String phone;

    @NotBlank
    @Size(min = 1, max = 100)
    private String address;

    @NotBlank
    @Size(min = 6, max = 6)
    @Pattern(regexp = "[0][1-9][0-9]{4}|[1-6][0-9]{5}|[7][012356789][0-9]{4}|[8][0-2][0-9]{4}")
    private String postalCode;

    @Min(0)
    @Max(110)
    private int age;

	@NotBlank
	@Pattern(regexp = "male|female")
    private String gender;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotNull
    @Size(min = 1)
    private List<@Pattern(regexp = "patient|therapist|administrator|researcher|external partner") String> roles;

    @NotBlank
    private String publicKey;
}
