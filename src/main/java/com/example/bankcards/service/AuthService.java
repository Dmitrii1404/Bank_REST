package com.example.bankcards.service;

import com.example.bankcards.dto.request.user.UserCreateRequest;
import com.example.bankcards.dto.request.user.UserLoginRequest;
import com.example.bankcards.dto.response.user.UserLoginResponse;
import com.example.bankcards.dto.response.user.UserResponse;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.InvalidCredentialsException;
import com.example.bankcards.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(UserCreateRequest userCreateRequest) {
        return userService.createUser(userCreateRequest);
    }

    @Transactional(readOnly = true)
    public UserLoginResponse login(UserLoginRequest userLoginRequest) {
        User user = userService.findByEmail(userLoginRequest.email());

        if (!passwordEncoder.matches(userLoginRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Неверный email или пароль");
        }

        return new UserLoginResponse(
                jwtTokenProvider.generateJwtToken(user.getEmail())
        );
    }
}
