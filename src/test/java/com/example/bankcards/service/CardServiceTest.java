package com.example.bankcards.service;

import com.example.bankcards.dto.request.card.CardCreateRequest;
import com.example.bankcards.dto.request.card.CardTransferRequest;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.EncryptCard;
import com.example.bankcards.util.NumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserService userService;

    @Mock
    private EncryptCard encryptCard;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Dmitrii")
                .secondName("Dmitrii")
                .email("dmitrii@gmail.com")
                .build();

        card = Card.builder()
                .id(1L)
                .user(user)
                .number("**** **** **** 1111")
                .expirationDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    @DisplayName("Найти все карты")
    void findAllCardsSuccess() {
        Page<Card> pageIn  = new PageImpl<>(List.of(card));
        Pageable pageable = PageRequest.of(0, 10);

        when(cardRepository.findAll(pageable)).thenReturn(pageIn);
        when(encryptCard.decrypt("**** **** **** 1111"))
                .thenReturn("1234 1234 1234 1234");

        Page<CardResponse> pageOut = cardService.findAllCards(pageable);

        assertEquals(1, pageOut.getTotalElements());
        CardResponse resp = pageOut.getContent().get(0);
        assertEquals("**** **** **** 1234", resp.cardNumber());
        assertEquals(1L, resp.id());
        assertEquals("Dmitrii", resp.firstName());
    }

    @Test
    @DisplayName("Найти все карты по email")
    void findCardsByEmailSuccess() {
        Page<Card> pageIn  = new PageImpl<>(List.of(card));
        Pageable  pageable = PageRequest.of(0, 5);

        when(userService.findByEmail("dmitrii@gmail.com")).thenReturn(user);
        when(cardRepository.findByUser(user, pageable)).thenReturn(pageIn);
        when(encryptCard.decrypt("**** **** **** 1111"))
                .thenReturn("1234 1234 1234 1234");

        Page<CardResponse> pageOut = cardService.findCardsByEmail("dmitrii@gmail.com", pageable);

        assertEquals(1, pageOut.getTotalElements());
        assertEquals("Dmitrii", pageOut.getContent().get(0).secondName());
    }

    @Test
    @DisplayName("Найти карту по ID - не являетесь владельцем")
    void findCardByEmailAndIdFail() {
        Card otherCard = Card.builder()
                .id(1L)
                .user(User.builder().id(2L).build())
                .number("**** **** **** 1111")
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(userService.findByEmail("dmitrii@gmail.com")).thenReturn(user);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(otherCard));

        CardOperationException ex = assertThrows(
                CardOperationException.class,
                () -> cardService.findCardByEmailAndId("dmitrii@gmail.com", 1L)
        );
        assertTrue(ex.getMessage().contains("Вы не являетесь владельцем"));
    }

    @Test
    @DisplayName("Баланс карты")
    void findCardBalanceSuccess() {
        when(userService.findByEmail("dmitrii@gmail.com")).thenReturn(user);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(encryptCard.decrypt("**** **** **** 1111"))
                .thenReturn("1234 1234 1234 1234");

        BigDecimal bal = cardService.findCardBalance("dmitrii@gmail.com", 1L);

        assertEquals(BigDecimal.valueOf(1000), bal);
    }

    @Test
    @DisplayName("Создать карту")
    void createCardSuccess() {
        try (MockedStatic<NumberGenerator> ng = mockStatic(NumberGenerator.class)) {
            ng.when(NumberGenerator::generateNumber).thenReturn("1234 1234 1234 1234");

            when(encryptCard.encrypt("1234 1234 1234 1234")).thenReturn("**** **** **** 1111");

            when(cardRepository.save(any(Card.class)))
                    .thenAnswer(inv -> {
                        Card c = inv.getArgument(0);
                        c.setId(1L);
                        return c;
                    });
            when(userService.findByEmail("dmitrii@gmail.com")).thenReturn(user);

            CardCreateRequest req = new CardCreateRequest("dmitrii@gmail.com");
            CardResponse resp = cardService.createCard(req);

            assertTrue(resp.cardNumber().endsWith("1234 1234 1234 1234".substring(15)));

            assertEquals("Dmitrii", resp.firstName());
            assertEquals(BigDecimal.valueOf(1000), resp.balance());
        }
    }

    @Test
    @DisplayName("Обновить карту")
    void updateStatusSuccess() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.updateStatus(1L, CardStatus.BLOCKED);

        verify(cardRepository).save(argThat(c -> c.getStatus() == CardStatus.BLOCKED));
    }

    @Test
    @DisplayName("Обновить карту - просрочена")
    void updateStatusFail() {
        card.setStatus(CardStatus.EXPIRED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        CardOperationException ex = assertThrows(
                CardOperationException.class,
                () -> cardService.updateStatus(1L, CardStatus.ACTIVE)
        );
        assertTrue(ex.getMessage().contains("Невозможно активировать просроченную карту"));
    }

    @Test
    @DisplayName("Удалить карту")
    void deleteCardSuccess() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.deleteCard(1L);

        verify(cardRepository).delete(card);
    }

    @Test
    @DisplayName("Удалить карту - не найдена")
    void deleteCardFail() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> cardService.deleteCard(1L)
        );
        assertTrue(ex.getMessage().contains("Карта не найдена. ID: 1"));
    }

    @Test
    @DisplayName("Перевести деньги")
    void transferMoneySuccess() {
        Card toCard = Card.builder()
                .id(2L)
                .user(user)
                .number("**** **** **** 1111")
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .build();

        when(userService.findByEmail("dmitrii@gmail.com")).thenReturn(user);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        CardTransferRequest req = new CardTransferRequest(1L, 2L, BigDecimal.valueOf(300));

        cardService.transferMoney("dmitrii@gmail.com", req);

        assertEquals(BigDecimal.valueOf(700), card.getBalance());
        assertEquals(BigDecimal.valueOf(800), toCard.getBalance());

        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    @DisplayName("Перевести деньги - не владелец карты")
    void transferMoneyFail() {
        User other = User.builder().id(99L).build();
        when(userService.findByEmail("dmitrii@gmail.com")).thenReturn(user);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(
                Card.builder().id(2L).user(other).status(CardStatus.ACTIVE).balance(BigDecimal.TEN).build()
        ));

        CardOperationException ex = assertThrows(
                CardOperationException.class,
                () -> cardService.transferMoney("dmitrii@gmail.com", new CardTransferRequest(1L, 2L, BigDecimal.ONE))
        );
        assertTrue(ex.getMessage().contains("Перевод возможен только между вашими картами"));
    }

    @Test
    @DisplayName("Перевести деньги - недостаточно средств")
    void transferMoneyFailAmount() {
        when(userService.findByEmail("dmitrii@gmail.com")).thenReturn(user);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(
                Card.builder().id(2L).user(user).status(CardStatus.ACTIVE).balance(BigDecimal.ZERO).build()
        ));

        CardOperationException ex = assertThrows(
                CardOperationException.class,
                () -> cardService.transferMoney("dmitrii@gmail.com", new CardTransferRequest(1L, 2L, BigDecimal.valueOf(2000)))
        );
        assertTrue(ex.getMessage().contains("Недостаточно средств"));
    }
}
