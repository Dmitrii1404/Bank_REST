package com.example.bankcards.dto.request.user;

import com.example.bankcards.entity.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateRequest(
        @NotBlank(message = "Имя не может быть пустым")
        String firstName,

        @NotBlank(message = "Фамилия не может быть пустым")
        String secondName,

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Неверный формат Email")
        String email,

        @NotBlank(message = "Пароль не может быть пустым")
        String password,

        @NotNull(message = "Роль не может быть пустой")
        Role role
) {}
