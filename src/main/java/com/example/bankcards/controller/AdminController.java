package com.example.bankcards.controller;


import com.example.bankcards.dto.request.user.UserUpdateRequest;
import com.example.bankcards.dto.response.user.UserResponse;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/admin/users")
@Tag(name = "User API for ADMIN", description = "Работа с картами пользователя. Для ADMIN")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @Operation(
            summary = "Получение всех пользователей",
            description = "Возвращает всех пользователей"
    )
    @ApiResponse(responseCode = "200",
            description = "Пользователи получены",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class)
            ))
    @GetMapping()
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            Pageable pageable
    ) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @Operation(
            summary = "Получение пользователя по ID",
            description = "Возвращает данные выбранного пользователя"
    )
    @ApiResponse(responseCode = "200",
            description = "Данные получены",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class)
            ))
    @ApiResponse(responseCode = "404",
            description = "Пользователь не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(
            summary = "Обновление данных пользователя",
            description = "Обновляет и возвращает данные выбранного пользователя"
    )
    @ApiResponse(responseCode = "200",
            description = "Данные изменены",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class)
            ))
    @ApiResponse(responseCode = "400",
            description = "Ошибка при работе с данными пользователя",
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
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userUpdateRequest));
    }

    @Operation(
            summary = "Удаление пользователя",
            description = "Удаляет выбранного пользователя"
    )
    @ApiResponse(responseCode = "200",
            description = "Пользователь удален")
    @ApiResponse(responseCode = "404",
            description = "Пользователь не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
