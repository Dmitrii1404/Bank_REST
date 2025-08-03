package com.example.bankcards.controller;


import com.example.bankcards.dto.request.card.CardTransferRequest;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardUserController.class)
@Import(CardUserControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class CardUserControllerTest {

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
    @DisplayName("Получение карт")
    @WithMockUser(roles = "USER")
    void getCardsSuccess() throws Exception {
        Page<CardResponse> response = new PageImpl<>(List.of(new CardResponse(1L, "**** **** **** 1111", "Dmitrii", "Dmitrii", LocalDate.now().plusYears(1), CardStatus.ACTIVE, new BigDecimal("500"))));
        Mockito.when(cardService.findCardsByEmail(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/cards"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение карты по ID")
    @WithMockUser(roles = "USER")
    void getCardByIdSuccess() throws Exception {
        CardResponse response = new CardResponse(1L, "**** **** **** 1111", "Dmitrii", "Dmitrii", LocalDate.now().plusYears(1), CardStatus.ACTIVE, new BigDecimal("500"));
        Mockito.when(cardService.findCardByEmailAndId(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/cards/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение баланса карты по ID")
    @WithMockUser(roles = "USER")
    void getBalanceSuccess() throws Exception {
        Mockito.when(cardService.findCardBalance(any(), any())).thenReturn(BigDecimal.valueOf(1000));

        mockMvc.perform(get("/api/v1/cards/1/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000"));
    }

    @Test
    @DisplayName("Получение запросов на блокировку")
    @WithMockUser(roles = "USER")
    void getRequestBlockSuccess() throws Exception {
        Page<BlockResponse> response = new PageImpl<>(List.of(new BlockResponse(1L, 1L, 1L, RequestStatus.COMPLETED, LocalDateTime.now())));
        Mockito.when(requestBlockService.findRequestByEmail(eq("test@gmail.com"), any())).thenReturn(response);
        mockMvc.perform(get("/api/v1/cards/request_block"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Создание запроса на блокировку")
    @WithMockUser(roles = "USER")
    void createRequestBlockSuccess() throws Exception {
        BlockResponse response = new BlockResponse(1L, 1L, 1L, RequestStatus.COMPLETED, LocalDateTime.now());
        Mockito.when(requestBlockService.createRequestBlock(eq("test@gmail.com"), any())).thenReturn(response);
        mockMvc.perform(post("/api/v1/cards/1/request_block"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Перевод между картами")
    @WithMockUser(roles = "USER")
    void transferSuccess() throws Exception {
        CardTransferRequest request = new CardTransferRequest(1L, 2L, new BigDecimal("1000"));

        mockMvc.perform(post("/api/v1/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(cardService).transferMoney(any(), any(CardTransferRequest.class));
    }
}
