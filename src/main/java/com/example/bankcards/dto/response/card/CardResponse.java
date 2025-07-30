package com.example.bankcards.dto.response.card;

import com.example.bankcards.entity.card.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse (
        Long id,
        String cardNumber,
        String firstName,
        String secondName,
        LocalDate expirationDate,
        CardStatus status,
        BigDecimal balance
) {}
