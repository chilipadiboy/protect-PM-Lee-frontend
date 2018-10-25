package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

@Component
public class AESEncryptionDecryptionTool {
    public byte[] encrypt(String toEncrypt, String secretKey, byte[] ivBytes, String transformation) {
        try {
            return encrypt(toEncrypt.getBytes("UTF-8"), secretKey, ivBytes, transformation);
        } catch (UnsupportedEncodingException uee) {
            throw new AssertionError("Encoding should be valid.");
        }
    }

    public byte[] encrypt(byte[] toEncrypt, String secretKey, byte[] ivBytes, String transformation) {
        try {
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            SecretKeySpec sKeySpec = new SecretKeySpec(Base64.getDecoder().decode(secretKey), "AES");
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);

            return cipher.doFinal(toEncrypt);
        } catch (Exception e) {
            throw new AssertionError("Should not happen.");
        }
    }

    public String decrypt(String toDecrypt, String secretKey, String ivString, String transformation) {
        try {
            IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(ivString));
            SecretKeySpec sKeySpec = new SecretKeySpec(Base64.getDecoder().decode(secretKey), "AES");
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(toDecrypt));

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AssertionError("Should not happen.");
        }
    }
}
