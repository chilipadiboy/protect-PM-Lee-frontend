package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.cs4239.team1.protectPMLeefrontendserver.exception.NonceExceededException;
import org.springframework.scheduling.annotation.Scheduled;

public class NonceGenerator {
    private static Map<String, Integer> nricToNonce = new HashMap<>();
    private static Map<String, Integer> nricToNumOfNonce = new HashMap<>();
    private static Map<String, LocalTime> nricToTime = new HashMap<>();

    /**
     * Generates a nonce for {@code nric} and returns it.
     */
    public static int generateNonce(String nric) throws NonceExceededException {
        if(nricToNumOfNonce.get(nric) == null) {
            int noncesIssued = 0;
            nricToNumOfNonce.put(nric, noncesIssued);
            nricToNonce.put(nric, 0);
        }
        if (nricToNumOfNonce.get(nric) > 30) {
            throw new NonceExceededException();
        }
        nricToTime.put(nric, LocalTime.now());

        return nricToNonce.get(nric);
    }

    public static int getNonce(String nric) {
        int storedNonce = nricToNonce.get(nric);
        return storedNonce;
    }

    public static void increaseNonce(String nric)  {
        int noncesIssued = nricToNumOfNonce.get(nric);
        noncesIssued++;
        nricToNumOfNonce.put(nric, noncesIssued);
        int nonce = nricToNonce.get(nric);
        nonce++;
        nricToNonce.put(nric, nonce);
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    private static void resetDailyNonceLimit() {
        for (String key : nricToNumOfNonce.keySet()) {
            nricToNumOfNonce.replace(key, 0);
        }
    }
}
