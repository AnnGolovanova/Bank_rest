package com.example.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Converter(autoApply = false)
public class EncryptingAttributeConverter implements AttributeConverter<String, String> {

    private final SecretKeySpec key;

    public EncryptingAttributeConverter(SecretKeySpec key) {
        this.key = key;
    }

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;
    private static final int TAG_LEN = 128;

    @Override
    public String convertToDatabaseColumn(String raw) {
        if (raw == null) return null;
        try {
            byte[] iv = java.security.SecureRandom.getInstanceStrong().generateSeed(IV_LEN);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));
            byte[] enc = cipher.doFinal(raw.getBytes(StandardCharsets.UTF_8));
            byte[] res = new byte[iv.length + enc.length];
            System.arraycopy(iv, 0, res, 0, iv.length);
            System.arraycopy(enc, 0, res, iv.length, enc.length);
            return Base64.getEncoder().encodeToString(res);
        } catch (Exception e) {
            throw new IllegalStateException("encrypt error", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String db) {
        if (db == null) return null;
        try {
            byte[] all = Base64.getDecoder().decode(db);
            byte[] iv = java.util.Arrays.copyOfRange(all, 0, IV_LEN);
            byte[] enc = java.util.Arrays.copyOfRange(all, IV_LEN, all.length);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));
            return new String(cipher.doFinal(enc), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("decrypt error", e);
        }
    }
}
