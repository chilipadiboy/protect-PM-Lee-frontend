package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtEncryptionDecryptionTool {
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    public byte[] encrypt(String jwt, byte[] ivBytes) {
        try {
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            SecretKeySpec sKeySpec = new SecretKeySpec(Base64.getDecoder().decode(jwtSecret), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);

            return cipher.doFinal(jwt.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new AssertionError("Should not happen.");
        }
    }

    public String decrypt(byte[] jwt, String ivString) {
        try {
            IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(ivString));
            SecretKeySpec sKeySpec = new SecretKeySpec(Base64.getDecoder().decode(jwtSecret), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(jwt));

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AssertionError("Should not happen.");
        }
    }
}
