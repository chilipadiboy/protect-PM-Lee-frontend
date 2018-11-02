package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoteResponse {
    private Long noteID;
    private String creatorNric;
    private String patientNric;
    private String noteContent;
}
