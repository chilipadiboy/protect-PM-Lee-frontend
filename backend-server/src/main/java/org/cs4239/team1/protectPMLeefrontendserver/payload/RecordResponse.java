package org.cs4239.team1.protectPMLeefrontendserver.payload;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecordResponse {
    private String recordID;
    private String type;
    private String subtype;
    private String title;
    private Instant date_time;
    private String document;
    private String patientIC;
}
