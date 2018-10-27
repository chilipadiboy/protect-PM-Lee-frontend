package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.Getter;

@Getter
public class NoteRequest {

    private String patientNric;

    private String noteContent;
}