package com.example.bankcards.dto.response.block;

import com.example.bankcards.entity.request.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BlockResponse(
        Long id,
        Long userId,
        Long cardId,
        RequestStatus status,
        LocalDateTime requestedAt
) {}
