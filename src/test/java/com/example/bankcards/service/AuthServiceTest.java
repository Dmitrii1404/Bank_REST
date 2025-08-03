package com.example.bankcards.service;

import com.example.bankcards.dto.request.user.UserCreateRequest;
import com.example.bankcards.dto.request.user.UserLoginRequest;
import com.example.bankcards.dto.response.user.UserLoginResponse;
import com.example.bankcards.dto.response.user.UserResponse;
import com.example.bankcards.entity.user.Role;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.InvalidCredentialsException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Регистрация пользователя")
    void registerSuccess() {
        UserCreateRequest request = new UserCreateRequest(
                "Dmitrii",
                "Dmitrii",
                "dmitrii@gmail.com",
                "dmitrii",
                Role.USER
        );
        UserResponse expected = new UserResponse(
                42L,
                "Dmitrii",
                "Dmitrii",
                "dmitrii@gmail.com",
                Role.USER
        );

        when(userService.createUser(request)).thenReturn(expected);
        UserResponse actual = authService.register(request);

        assertEquals(expected, actual);
        verify(userService, times(1)).createUser(request);
    }

    @Test
    @DisplayName("Вход в аккаунт")
    void loginSuccess() {
        UserLoginRequest request = new UserLoginRequest("dmitrii@gmail.com", "dmitrii");
        User user = User.builder()
                .email("dmitrii@gmail.com")
                .password("dmitriiHash")
                .build();

        when(userService.findByEmail("dmitrii@gmail.com")).thenReturn(user);
        when(passwordEncoder.matches("dmitrii", "dmitriiHash")).thenReturn(true);
        when(jwtTokenProvider.generateJwtToken("dmitrii@gmail.com"))
                .thenReturn("token");

        UserLoginResponse resp = authService.login(request);

        assertNotNull(resp);
        assertEquals("token", resp.token());

        verify(userService).findByEmail("dmitrii@gmail.com");
        verify(passwordEncoder).matches("dmitrii", "dmitriiHash");
        verify(jwtTokenProvider).generateJwtToken("dmitrii@gmail.com");
    }

    @Test
    @DisplayName("Вход в аккаунт — пользователь не найден")
    void loginFailNotFound() {
        UserLoginRequest request = new UserLoginRequest("nikita@gmail.com", "nikita");

        when(userService.findByEmail("nikita@gmail.com"))
                .thenThrow(new NotFoundException("Пользователь не найден. Email: nikita@gmail.com"));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> authService.login(request)
        );
        assertTrue(ex.getMessage().contains("Пользователь не найден. Email: nikita@gmail.com"));

        verify(userService).findByEmail("nikita@gmail.com");
    }

    @Test
    @DisplayName("Вход в аккаунт — неверный пароль")
    void loginFailBadPassword() {
        UserLoginRequest request = new UserLoginRequest("dmitrii@gmail.com", "nikita");
        User user = User.builder()
                .email("dmitrii@gmail.com")
                .password("dmitriiHash")
                .build();

        when(userService.findByEmail("dmitrii@gmail.com")).thenReturn(user);
        when(passwordEncoder.matches("nikita", "dmitriiHash")).thenReturn(false);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request)
        );
        assertTrue(ex.getMessage().contains("Неверный email или пароль"));

        verify(userService).findByEmail("dmitrii@gmail.com");
        verify(passwordEncoder).matches("nikita", "dmitriiHash");
    }
}
