package com.example.bankcards.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
public class CryptoConfig {
    @Bean
    public SecretKeySpec cardKey() {
        byte[] key = Arrays.copyOf(
                "dev-secret-32-bytes-minimum-!!!".getBytes(StandardCharsets.UTF_8), 32);
        return new SecretKeySpec(key, "AES");
    }
}
