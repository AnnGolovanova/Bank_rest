package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import jakarta.validation.constraints.Future;


import java.time.LocalDate;

public record CardUpdateRequest(
        String holderName,
@Future
LocalDate expiry,
        CardStatus status
        ) {}