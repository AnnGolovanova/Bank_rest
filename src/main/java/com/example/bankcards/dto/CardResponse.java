package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(
        Long id,
        String maskedNumber,
        String holderName,
        LocalDate expiry,
        CardStatus status,
        BigDecimal balance
        ) {}
