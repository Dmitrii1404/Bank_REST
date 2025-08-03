package com.example.bankcards.controller;

import com.example.bankcards.dto.request.user.UserUpdateRequest;
import com.example.bankcards.dto.response.user.UserResponse;
import com.example.bankcards.entity.user.Role;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.security.UserDetailsServiceCustom;
import com.example.bankcards.service.UserService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AdminController.class)
@Import(AdminControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
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
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Получение всех пользователей")
    void getAllUsersSuccess() throws Exception {
        UserResponse response = new UserResponse(1L, "Dmitrii", "Dmitrii", "dmitrii@gmail.com", Role.USER);
        Page<UserResponse> page = new PageImpl<>(List.of(response));

        Mockito.when(userService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(response.id()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Получение пользователя по ID")
    void getUserByIdSuccess() throws Exception {
        UserResponse response = new UserResponse(1L, "Dmitrii", "Dmitrii", "dmitrii@gmail.com", Role.USER);

        Mockito.when(userService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Dmitrii"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Обновление данных пользователя")
    void updateUserSuccess() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest(null, "Dima", null, null);
        UserResponse response = new UserResponse(1L, "Dmitrii", "Dima", "dmitrii@gmail.com", Role.USER);

        Mockito.when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Dmitrii"))
                .andExpect(jsonPath("$.secondName").value("Dima"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Удаление пользователя")
    void deleteUserSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isOk());

        Mockito.verify(userService).deleteUser(1L);
    }
}
