package com.example.bankcards.dto.request.card;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CardTransferRequest(

        @NotNull(message = "ID карты отправителя не может быть пустым")
        Long fromCardId,

        @NotNull(message = "ID карты получателя не может быть пустым")
        Long toCardId,

        @NotNull(message = "Amount не может быть пустым")
        BigDecimal amount
) {
}
