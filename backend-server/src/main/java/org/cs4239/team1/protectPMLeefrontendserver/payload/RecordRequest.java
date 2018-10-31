package org.cs4239.team1.protectPMLeefrontendserver.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;

@Getter
public class RecordRequest {

    @NotBlank
    @Size(max = 140)
    @Pattern(regexp = "reading|illness")
    private String type;

    @NotBlank
    @Size(max = 140)
    @Pattern(regexp = "allergy|cold|headaches and migraines|asthma|high blood cholesterol|cancer|diabetes|heart disease|" +
            "hypertension|stroke|panic attack|depression|eating disorders|obsessive compulsive disorder|schizophrenia|" +
            "bronchitis|back pain|cataracts|caries|chickenpox|gingivitis|gout|haemorrhoids|urinary|blood pressure|cholesterol")
    private String subtype;

    @NotBlank
    @Size(max = 140)
    private String title;

    @NotBlank
    @Size(max = 140)
    private String patientIC;
}