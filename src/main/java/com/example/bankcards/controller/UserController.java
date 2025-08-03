package com.example.bankcards.controller;


import com.example.bankcards.dto.request.user.UserUpdatePassword;
import com.example.bankcards.dto.request.user.UserUpdateRequest;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.security.UserDetailsCustom;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/users")
@Tag(name = "User API", description = "Изменение данных пользователя. Для всех ролей")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Обновление пароля",
            description = "Обновляет пароль у текущего пользователя"
    )
    @ApiResponse(responseCode = "200",
            description = "Пароль обновлен")
    @ApiResponse(responseCode = "404",
            description = "Пользователь не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @PutMapping("/update_password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdatePassword userUpdatePassword
    ) {

        userService.updatePassword(userDetails.getUsername(), userUpdatePassword);

        return ResponseEntity.ok().build();
    }
}
