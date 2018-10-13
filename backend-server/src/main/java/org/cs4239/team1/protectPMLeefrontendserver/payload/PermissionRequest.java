package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionRequest {

    private Long recordID;

    private String nric;

    private int months;

    private int days;
}