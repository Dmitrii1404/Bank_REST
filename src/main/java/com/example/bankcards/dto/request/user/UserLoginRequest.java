package com.example.bankcards.dto.request.user;

public record UserLoginRequest(
        String email,
        String password
) {}
