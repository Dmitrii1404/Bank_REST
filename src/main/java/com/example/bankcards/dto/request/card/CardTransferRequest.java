package com.example.bankcards.dto.request.card;

import java.math.BigDecimal;

public record CardTransferRequest(
        Long fromCardId,
        Long toCardId,
        BigDecimal amount
) {
}
