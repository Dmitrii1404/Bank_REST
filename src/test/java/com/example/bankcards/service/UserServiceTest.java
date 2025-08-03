package com.example.bankcards.service;

import com.example.bankcards.dto.request.user.UserCreateRequest;
import com.example.bankcards.dto.request.user.UserUpdatePassword;
import com.example.bankcards.dto.request.user.UserUpdateRequest;
import com.example.bankcards.dto.response.user.UserResponse;
import com.example.bankcards.entity.user.Role;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Найти всех пользователей")
    void findAllSuccess() {
        User user = User.builder()
                .id(1L)
                .firstName("Dmitrii")
                .secondName("Dmitrii")
                .email("Dmitrii@gmail.com")
                .role(Role.USER)
                .build();
        Page<User> page = new PageImpl<>(List.of(user));
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<UserResponse> result = userService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        UserResponse resp = result.getContent().get(0);
        assertEquals(1L, resp.id());
        assertEquals("Dmitrii", resp.firstName());
    }

    @Test
    @DisplayName("Найти по ID")
    void findByIdSuccess() {
        User user = User.builder()
                .id(1L)
                .firstName("Dmitrii")
                .secondName("Dmitrii")
                .email("Dmitrii@gmail.com")
                .role(Role.USER)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("Dmitrii", response.firstName());
        assertEquals("Dmitrii", response.secondName());
        assertEquals(Role.USER, response.role());
    }

    @Test
    @DisplayName("Найти по ID — пользователь не найден")
    void findByIdFail() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.findById(1L)
        );
        assertTrue(ex.getMessage().contains("Пользователь не найден. Id: 1"));
    }

    @Test
    @DisplayName("Найти по Email")
    void findByEmailSuccess() {
        User user = User.builder()
                .id(1L)
                .email("dmitrii@gmail.com")
                .build();

        when(userRepository.findByEmail("dmitrii@gmail.com"))
                .thenReturn(Optional.of(user));

        User found = userService.findByEmail("dmitrii@gmail.com");
        assertEquals(1L, found.getId());
    }

    @Test
    @DisplayName("Найти по Email — пользователь не найден")
    void findByEmailFail() {
        when(userRepository.findByEmail("nikita@gmail.com"))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.findByEmail("nikita@gmail.com")
        );
        assertTrue(ex.getMessage().contains("Пользователь не найден. Email: nikita@gmail.com"));
    }

    @Test
    @DisplayName("Создать пользователя")
    void createUserSuccess() {
        UserCreateRequest request = new UserCreateRequest(
                "Dmitrii",
                "Dmitrii",
                "dmitrii@gmail.com",
                "dmitrii",
                Role.USER
        );

        when(userRepository.existsByEmail("dmitrii@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("dmitrii")).thenReturn("dmitriiHash");

        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> {
                    User user = inv.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        UserResponse response = userService.createUser(request);

        assertEquals(1L, response.id());
        assertEquals("Dmitrii", response.firstName());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("dmitriiHash", captor.getValue().getPassword());
    }

    @Test
    @DisplayName("Создать пользователя — email уже существует")
    void createUserFail() {
        UserCreateRequest request = new UserCreateRequest(
                "Dmitrii",
                "Dmitrii",
                "dmitrii@gmail.com",
                "dmitrii",
                Role.USER
        );

        when(userRepository.existsByEmail("dmitrii@gmail.com")).thenReturn(true);

        UserOperationException ex = assertThrows(
                UserOperationException.class,
                () -> userService.createUser(request)
        );
        assertTrue(ex.getMessage().contains("Пользователь с email dmitrii@gmail.com уже существует"));
    }

    @Test
    @DisplayName("Обновление данных пользователя")
    void updateUserSuccess() {
        User user = User.builder()
                .id(1L)
                .firstName("Dmitrii")
                .secondName("Dmitrii")
                .email("dmitrii@gmail.com")
                .password("dmitrii")
                .role(Role.USER)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("newPass")).thenReturn("newHash");

        var req = new UserUpdateRequest(
                "NewFirst",
                null,
                null,
                "newPass"
        );

        UserResponse resp = userService.updateUser(1L, req);

        assertEquals("NewFirst", resp.firstName());
        verify(userRepository).save(user);
        assertEquals("newHash", user.getPassword());
    }

    @Test
    @DisplayName("Обновление данных — пользователь не найден")
    void updateUserFail() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());
        UserUpdateRequest request = new UserUpdateRequest(
                "Dmitrii",
                null,
                null,
                null);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(1L, request)
        );
        assertTrue(ex.getMessage().contains("Пользователь не найден. Id: 1"));
    }

    @Test
    @DisplayName("Обновление пароля")
    void updatePasswordSuccess() {
        User user = User.builder()
                .id(1L)
                .email("dmitrii@gmail.com")
                .password("dmitriiHash")
                .build();
        when(userRepository.findByEmail("dmitrii@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("newPassHash");

        userService.updatePassword("dmitrii@gmail.com", new UserUpdatePassword("newPass"));

        assertEquals("newPassHash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUserSuccess() {
        User user = User.builder()
                .id(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("Удаление пользователя — не найден")
    void deleteUserFail() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.deleteUser(1L)
        );
        assertTrue(ex.getMessage().contains("Пользователь не найден. Id: 1"));
    }
}
