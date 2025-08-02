package com.example.bankcards.dto.request.card;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CardCreateRequest(

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Неверный формат Email. Ожидается example@gmail.com")
        String email
) {}
