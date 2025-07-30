package com.example.bankcards.dto.request.user;

public record UserUpdateRequest (
        String firstName,
        String secondName,
        String email,
        String password
) {}
