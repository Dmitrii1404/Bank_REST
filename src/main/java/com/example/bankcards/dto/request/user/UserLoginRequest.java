package com.example.bankcards.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Неверный формат Email. Ожидается example@gmail.com")
        String email,

        @NotBlank(message = "Пароль не может быть пустым")
        String password
) {}
