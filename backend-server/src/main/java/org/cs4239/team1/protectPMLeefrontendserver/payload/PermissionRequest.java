package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class PermissionRequest {

    private Long recordID;

    private String nric;

    private String endDate;
}