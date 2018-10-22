package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.Getter;

@Getter
//TODO Requests should have some kind input verification
public class TreatmentRequest {

    private String therapistNric;

    private String patientNric;

    private String endDate;
}