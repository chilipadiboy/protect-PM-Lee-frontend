package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.Getter;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
public class EndTreatmentRequest {

    @NonNull
    @NotBlank
    @Size(min = 9, max = 9)
    private String therapistNric;

    @NonNull
    @NotBlank
    @Size(min = 9, max = 9)
    private String patientNric;
}