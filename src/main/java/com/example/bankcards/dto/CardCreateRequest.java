package com.example.bankcards.dto;


import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CardCreateRequest(
@NotBlank
String cardNumber,
@NotBlank
String holderName,
@NotNull
@Future
LocalDate expiry,
@NotNull
@DecimalMin("0.00")
BigDecimal initialBalance,
@NotNull
Long ownerId
        ) {}