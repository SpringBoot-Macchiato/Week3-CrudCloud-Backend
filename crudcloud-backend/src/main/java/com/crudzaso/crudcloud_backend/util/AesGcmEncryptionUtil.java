package com.crudzaso.crudcloud_backend.util;

import jakarta.xml.bind.DatatypeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM encrypt/decrypt utility.
 * Requires a 32-byte (256-bit) key in environment or properties (base64 encoded or raw).
 *
 * NOTE: keep AES key in environment variable, not in code.
 */
@Component
public class AesGcmEncryptionUtil {

    private static final int IV_LENGTH = 12; // 96 bits for GCM
    private static final int TAG_LENGTH_BIT = 128;

    private final byte[] keyBytes;

    public AesGcmEncryptionUtil(@Value("${app.crypto.key}") String secret) {
        // secret should be base64 or raw string; here we accept raw UTF-8 bytes
        // Convert to 32 bytes: if secret shorter, pad; if longer, truncate — better to provide a 32-byte secret.
        byte[] src = secret.getBytes();
        keyBytes = new byte[32];
        System.arraycopy(src, 0, keyBytes, 0, Math.min(src.length, 32));
    }

    public String encrypt(String plain) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

        byte[] cipherText = cipher.doFinal(plain.getBytes());
        // return base64(iv + cipherText)
        byte[] out = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(cipherText, 0, out, iv.length, cipherText.length);
        return Base64.getEncoder().encodeToString(out);
    }

    public String decrypt(String base64IvAndCipher) throws Exception {
        byte[] all = Base64.getDecoder().decode(base64IvAndCipher);
        byte[] iv = new byte[IV_LENGTH];
        byte[] cipherText = new byte[all.length - IV_LENGTH];
        System.arraycopy(all, 0, iv, 0, IV_LENGTH);
        System.arraycopy(all, IV_LENGTH, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        byte[] plain = cipher.doFinal(cipherText);
        return new String(plain);
    }
}
