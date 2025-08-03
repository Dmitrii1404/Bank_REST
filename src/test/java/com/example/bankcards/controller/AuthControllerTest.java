package com.example.bankcards.controller;


import com.example.bankcards.dto.request.user.UserCreateRequest;
import com.example.bankcards.dto.request.user.UserLoginRequest;
import com.example.bankcards.dto.response.user.UserLoginResponse;
import com.example.bankcards.dto.response.user.UserResponse;
import com.example.bankcards.entity.user.Role;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.security.UserDetailsServiceCustom;
import com.example.bankcards.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthService authService() {
            return Mockito.mock(AuthService.class);
        }
        @Bean
        public JwtTokenProvider jwtTokenProvider() {
            return Mockito.mock(JwtTokenProvider.class);
        }
        @Bean
        public UserDetailsServiceCustom userDetailsService() {
            return Mockito.mock(UserDetailsServiceCustom.class);
        }
    }

    @Test
    @DisplayName("Регистрация пользователя")
    void registerSuccess() throws Exception {
        UserCreateRequest request = new UserCreateRequest("Dmitrii", "Dmitrii", "dmitrii@gmail.com", "dmitrii", Role.ADMIN);
        UserResponse response = new UserResponse(1L, "Dmitrii", "Dmitrii", "dmitrii@gmail.com", Role.ADMIN);

        Mockito.when(authService.register(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(response.firstName()))
                .andExpect(jsonPath("$.secondName").value(response.secondName()))
                .andExpect(jsonPath("$.email").value(response.email()))
                .andExpect(jsonPath("$.role").value(response.role().name()));
    }

    @Test
    @DisplayName("Ошибка при регистрации")
    void registerFail() throws Exception {
        String body = """
        {
          "firstName":"Dmitrii",
          "secondName":"Dmitrii",
          "password":"dmitrii",
          "role":"ADMIN"
        }
        """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error", containsString("Email")));
    }


    @Test
    @DisplayName("Вход в аккаунт")
    void loginSuccess() throws Exception {
        UserLoginRequest request = new UserLoginRequest("dmitrii@gmail.com", "dmitrii");
        UserLoginResponse response = new UserLoginResponse("token");

        Mockito.when(authService.login(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(response.token()));
    }

}
