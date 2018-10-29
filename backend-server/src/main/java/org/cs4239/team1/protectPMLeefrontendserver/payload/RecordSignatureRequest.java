package org.cs4239.team1.protectPMLeefrontendserver.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;

@Getter
public class RecordSignatureRequest {
    @NotBlank
    private String encryptedString;

    @NotBlank
    private String iv;
}
