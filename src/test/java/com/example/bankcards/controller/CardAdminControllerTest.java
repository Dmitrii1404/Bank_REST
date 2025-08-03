package com.example.bankcards.controller;


import com.example.bankcards.dto.request.card.CardCreateRequest;
import com.example.bankcards.dto.response.block.BlockResponse;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.request.RequestStatus;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.security.UserDetailsServiceCustom;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.RequestBlockService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardAdminController.class)
@Import(CardAdminControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class CardAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardService cardService;

    @Autowired
    private RequestBlockService requestBlockService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CardService cardService() {
            return Mockito.mock(CardService.class);
        }
        @Bean
        public RequestBlockService requestBlockService() {
            return Mockito.mock(RequestBlockService.class);
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
    @DisplayName("Получение всех карт")
    void getAllCardsSuccess() throws Exception {
        CardResponse card = new CardResponse(1L, "**** **** **** 1111", "Dmitrii", "Dmitrii", LocalDate.now().plusYears(1), CardStatus.ACTIVE, new BigDecimal(1000));
        Page<CardResponse> page = new PageImpl<>(List.of(card));

        Mockito.when(cardService.findAllCards(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(card.id()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Получение всех запросов на блокировку")
    void getAllRequestBlockSuccess() throws Exception {
        BlockResponse blockResponse = new BlockResponse(1L, 1L, 1L, RequestStatus.NOT_STARTED, LocalDateTime.now());
        Page<BlockResponse> page = new PageImpl<>(List.of(blockResponse));

        Mockito.when(requestBlockService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/cards/request_block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(blockResponse.id()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Создание карты")
    void createCardSuccess() throws Exception {
        CardCreateRequest request = new CardCreateRequest("test@gmail.com");
        CardResponse response = new CardResponse(1L, "**** **** **** 1111", "Dmitrii", "Dmitrii", LocalDate.now().plusYears(1), CardStatus.ACTIVE, new BigDecimal(1000));

        Mockito.when(cardService.createCard(any(CardCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.cardNumber").value("**** **** **** 1111"))
                .andExpect(jsonPath("$.firstName").value("Dmitrii"))
                .andExpect(jsonPath("$.secondName").value("Dmitrii"))
                .andExpect(jsonPath("$.balance").value(1000))
                .andExpect(jsonPath("$.expirationDate").value(LocalDate.now().plusYears(1).toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Выполнить запрос")
    void completeRequestBlockSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/admin/cards/complete_request/1"))
                .andExpect(status().isOk());

        Mockito.verify(requestBlockService).completeRequestBlock(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Изменить статус карты")
    void updateStatusSuccess() throws Exception {
        mockMvc.perform(put("/api/v1/admin/cards/status/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CardStatus.BLOCKED)))
                .andExpect(status().isOk());

        Mockito.verify(cardService).updateStatus(eq(1L), any(CardStatus.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Удаление карты")
    void deleteCardSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/cards/1"))
                .andExpect(status().isOk());

        Mockito.verify(cardService).deleteCard(1L);
    }
}
