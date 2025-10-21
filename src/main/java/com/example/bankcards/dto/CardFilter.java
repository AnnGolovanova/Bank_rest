package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record CardFilter(
        CardStatus status,
@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryBefore,
        String holderNameLike
        ) {}
