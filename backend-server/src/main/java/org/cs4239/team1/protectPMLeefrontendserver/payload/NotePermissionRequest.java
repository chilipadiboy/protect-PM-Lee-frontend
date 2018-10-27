package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.Getter;

@Getter
public class NotePermissionRequest {

    private long noteID;
    private String isVisibleToPatient;
}