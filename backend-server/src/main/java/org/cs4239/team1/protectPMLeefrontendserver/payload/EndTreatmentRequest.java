package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.Getter;

@Getter
//TODO Requests should have some kind input verification NOTBLANKetc
public class EndTreatmentRequest {

    private String therapistNric;

    private String patientNric;
}