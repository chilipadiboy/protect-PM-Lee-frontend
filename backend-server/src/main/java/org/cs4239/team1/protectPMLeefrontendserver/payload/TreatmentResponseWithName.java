package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.cs4239.team1.protectPMLeefrontendserver.model.TreatmentId;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class TreatmentResponseWithName {

    private TreatmentId treatmentId;
    private String therapistName;
    private String patientNric;
    private String patientName;
    private Instant endDate;

}
