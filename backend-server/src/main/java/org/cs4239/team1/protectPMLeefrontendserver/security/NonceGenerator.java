package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.util.HashMap;
import java.util.Map;

public class NonceGenerator {
    private static int nonce = 0;
    private static Map<String, Integer> nricToNonce = new HashMap<>(); //TODO: Periodically clear map

    /**
     * Generates a nonce for {@code nric} and returns it.
     */
    public static int generateNonce(String nric) {
        nricToNonce.put(nric, nonce);
        return nonce++;
    }

    public static int getNonce(String nric) {
        int storedNonce = nricToNonce.get(nric);
        nricToNonce.remove(nric);
        return storedNonce;
    }
}
