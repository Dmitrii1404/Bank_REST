package com.example.bankcards.service;


import com.example.bankcards.dto.request.user.UserCreateRequest;
import com.example.bankcards.dto.request.user.UserUpdateRequest;
import com.example.bankcards.dto.response.user.UserResponse;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::response)
                .toList();
    }

    public UserResponse findById(Long id) {
        User user = findUserById(id);

        return response(user);
    }

    public User findByEmail(String email) {
        return userRepository.
                findByEmail(email).
                orElseThrow(() -> new NotFoundException("Пользователь не найден. Email: " + email));
    }

    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        if (userRepository.existsByEmail(userCreateRequest.email())) {
            throw new UserOperationException("Пользователь с email " + userCreateRequest.email() + " уже существует");
        }

        User user = User.builder()
                .firstName(userCreateRequest.firstName())
                .secondName(userCreateRequest.secondName())
                .email(userCreateRequest.email())
                .password(passwordEncoder.encode(userCreateRequest.password()))
                .role(userCreateRequest.role())
                .build();

        userRepository.save(user);

        return response(user);
    }



    public UserResponse updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден. Id: " + id));

        if (userUpdateRequest.firstName() != null) {
            user.setFirstName(userUpdateRequest.firstName());
        }
        if (userUpdateRequest.secondName() != null) {
            user.setSecondName(userUpdateRequest.secondName());
        }
        if (userUpdateRequest.email() != null) {
            user.setEmail(userUpdateRequest.email());
        }
        if (userUpdateRequest.password() != null) {
            user.setPassword(passwordEncoder.encode(userUpdateRequest.password()));
        }

        userRepository.save(user);

        return response(user);
    }

    public void deleteUser(Long id) {
        User user = findUserById(id);

        userRepository.delete(user);
    }

    private UserResponse response(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getSecondName(),
                user.getEmail(),
                user.getRole()
        );
    }

    private User findUserById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден. Id: " + id));
    }
}
