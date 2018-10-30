package org.cs4239.team1.protectPMLeefrontendserver.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import lombok.Getter;

@Getter
public class AnonymisedRecordRequest {

    @NotBlank
    @Pattern(regexp = "all|south|south-west|central|north|north-east|west|north-west|east")
    private String location;

    @NotBlank
    @Pattern(regexp = "all|below 13|from 13 to 18|from 19 to 25|from 26 to 35|from 36 to 55|above 55")
    private String age;

    @NotBlank
    @Pattern(regexp = "all|male|female")
    private String gender;

    @NotBlank
    @Pattern(regexp = "all|allergy|cold|headaches and migraines|asthma|high blood cholesterol|cancer|diabetes|heart disease|" +
            "hypertension|stroke|panic attack|depression|eating disorders|obsessive compulsive disorder|schizophrenia|" +
            "bronchitis|back pain|cataracts|caries|chickenpox|gingivitis|gout|haemorrhoids|urinary|blood pressure|cholesterol")
    private String subtype;

    @NotBlank
    @Pattern(regexp = "illness|reading")
    private String type;
}
