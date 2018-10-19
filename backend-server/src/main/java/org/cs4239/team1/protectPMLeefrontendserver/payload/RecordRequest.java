package org.cs4239.team1.protectPMLeefrontendserver.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;

@Getter
public class RecordRequest {

    @NotBlank
    @Size(max = 140)
    private String type;

    @NotBlank
    @Size(max = 140)
    private String subtype;

    @NotBlank
    @Size(max = 140)
    private String title;

    @NotBlank
    @Size(max = 140)
    private String document;

    @NotBlank
    @Size(max = 140)
    private String patientIC;
}