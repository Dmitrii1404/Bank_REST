package com.example.bankcards.dto.response.user;


import com.example.bankcards.entity.user.Role;

public record UserResponse (
        Long id,
        String firstName,
        String secondName,
        String email,
        Role role
){}
