package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service @RequiredArgsConstructor
public class TransferService {
    private final CardRepository cards;

    @Transactional
    public void transfer(Long ownerId, Long fromId, Long toId, BigDecimal amount) {
        if (fromId.equals(toId)) throw new IllegalArgumentException("та же карта");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("сумма");

        Card from = cards.findByIdAndOwner_Id(fromId, ownerId).orElseThrow();
        Card to   = cards.findByIdAndOwner_Id(toId, ownerId).orElseThrow();

        validate(from);
        validate(to);

        if (from.getBalance().compareTo(amount) < 0) throw new IllegalStateException("не достаточно средств");

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
    }

    private void validate(Card c){
        if (c.getStatus() != CardStatus.ACTIVE) throw new IllegalStateException("карта не активна");
        if (c.getExpiry().isBefore(LocalDate.now())) throw new IllegalStateException("срок карты истек");
    }
}
