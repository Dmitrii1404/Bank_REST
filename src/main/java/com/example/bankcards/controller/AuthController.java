package com.example.bankcards.controller;


import com.example.bankcards.dto.request.user.UserCreateRequest;
import com.example.bankcards.dto.request.user.UserLoginRequest;
import com.example.bankcards.dto.response.user.UserLoginResponse;
import com.example.bankcards.dto.response.user.UserResponse;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@Tag(name = "Authorization API", description = "Авторизация пользователя")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Регистрация пользователя",
            description = "Создает нового пользователя и возвращает данные этого пользователя"
    )
    @ApiResponse(responseCode = "200",
            description = "Пользователь создан",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class)
            ))
    @ApiResponse(responseCode = "400",
            description = "Ошибка создание пользователя",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        return ResponseEntity.ok(
                authService.register(userCreateRequest)
        );
    }

    @Operation(
            summary = "Вход в аккаунт",
            description = "Выполняет вход по логину и паролю и возвращает токен"
            )
    @ApiResponse(
            responseCode = "200",
            description = "Пользователь вошел в аккаунт",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserLoginResponse.class)
            ))
    @ApiResponse(responseCode = "401",
            description = "Неверный логин или пароль",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @ApiResponse(responseCode = "404",
            description = "Пользователь не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        return ResponseEntity.ok(
                authService.login(userLoginRequest)
        );
    }
}
