package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
@NotNull Long fromCardId,
@NotNull Long toCardId,
@NotNull @DecimalMin("0.01") BigDecimal amount
        ) {}
