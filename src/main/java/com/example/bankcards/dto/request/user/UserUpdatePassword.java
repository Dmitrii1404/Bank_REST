package com.example.bankcards.dto.request.user;

import jakarta.validation.constraints.NotBlank;

public record UserUpdatePassword(

        @NotBlank(message = "Пароль не может быть пустым")
        String newPassword
) {}
