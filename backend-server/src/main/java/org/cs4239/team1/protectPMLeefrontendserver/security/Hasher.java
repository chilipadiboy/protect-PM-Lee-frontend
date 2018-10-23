package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public class Hasher {
    private static MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-512");
        } catch (Exception e) {
            throw new AssertionError("Algorithm should exist.");
        }
    }

    public static byte[] hash(int toHash) {
        try {
            return digest.digest(Integer.toString(toHash).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException uee) {
            throw new AssertionError("Encoding should be valid.");
        }
    }
}
