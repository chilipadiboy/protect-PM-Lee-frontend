package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerSignatureResponse {
    private byte[] messageHash;
    private byte[] signature;
}
