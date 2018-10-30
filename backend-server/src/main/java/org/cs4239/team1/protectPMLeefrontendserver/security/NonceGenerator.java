package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.cs4239.team1.protectPMLeefrontendserver.exception.NonceExceededException;
import org.springframework.scheduling.annotation.Scheduled;

public class NonceGenerator {
    private static int nonce = 0;
    private static int noncesIssued = 0;
    private static Map<String, Integer> nricToNonce = new HashMap<>();
    private static Map<String, LocalTime> nricToTime = new HashMap<>();

    /**
     * Generates a nonce for {@code nric} and returns it.
     */
    public static int generateNonce(String nric) throws NonceExceededException {
        if (noncesIssued > 30) {
            throw new NonceExceededException();
        }

        nricToNonce.put(nric, nonce);
        nricToTime.put(nric, LocalTime.now());
        noncesIssued++;
        return nonce;
    }

    public static int getNonce(String nric) {
        int storedNonce = nricToNonce.get(nric);
        nricToNonce.remove(nric);
        nricToTime.remove(nric);
        return storedNonce;
    }

    public static void increaseNonce(String nric)  {
        nonce++;
    }

    @Scheduled(fixedRate = 1000 * 60)
    private static void clearMap() {
        LocalTime now = LocalTime.now();
        for (String nric : nricToNonce.keySet()) {
            if (Duration.between(nricToTime.get(nric), now).getSeconds() <= 60) {
                continue;
            }

            nricToNonce.remove(nric);
            nricToTime.remove(nric);
        }
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    private static void resetDailyNonceLimit() {
        noncesIssued = 0;
    }
}
