package com.example.bankcards.controller;


import com.example.bankcards.dto.request.user.UserCreateRequest;
import com.example.bankcards.dto.request.user.UserLoginRequest;
import com.example.bankcards.dto.response.user.UserLoginResponse;
import com.example.bankcards.dto.response.user.UserResponse;
import com.example.bankcards.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        return ResponseEntity.ok(
                authService.register(userCreateRequest)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        return ResponseEntity.ok(
                authService.login(userLoginRequest)
        );
    }
}
