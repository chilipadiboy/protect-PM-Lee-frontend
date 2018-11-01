package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerSignatureResponse {
    private byte[] iv;
    private byte[] combined;
    private byte[] signature;
}
