package com.example.bankcards.entity;

import com.example.bankcards.util.EncryptingAttributeConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = EncryptingAttributeConverter.class)
    @Column(name = "card_number_enc", nullable = false, unique = true)
    private String cardNumberEnc; // хранится шифром, конвертер вернёт расшифровку в entity

    @Column(nullable = false)
    private String holderName;
    @Column(nullable = false)
    private LocalDate expiry;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="owner_id", nullable = false)
    private User owner;

    @Transient
    public String maskedNumber() {
        String raw = getCardNumberEnc(); // уже расшифрован конвертером
        String digits = raw.replaceAll("\\D+", "");
        String last4 = digits.length() >= 4 ? digits.substring(digits.length()-4) : digits;
        return "**** **** **** " + last4;
    }
}
