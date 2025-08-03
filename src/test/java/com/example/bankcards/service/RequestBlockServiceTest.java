package com.example.bankcards.service;

import com.example.bankcards.dto.response.block.BlockResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.request.RequestBlock;
import com.example.bankcards.entity.request.RequestStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RequestBlockRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestBlockServiceTest {

    @Mock
    private RequestBlockRepository requestBlockRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RequestBlockService requestBlockService;

    private User user;
    private Card card;
    private RequestBlock block;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("dmitrii@gmail.com");

        card = new Card();
        card.setId(1L);
        card.setUser(user);
        card.setStatus(CardStatus.ACTIVE);

        block = new RequestBlock();
        block.setId(1L);
        block.setUser(user);
        block.setCard(card);
        block.setStatus(RequestStatus.NOT_STARTED);
        block.setRequestedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Найти все запросы")
    void findAllSuccess() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RequestBlock> pageIn = new PageImpl<>(List.of(block));

        when(requestBlockRepository.findAll(pageable)).thenReturn(pageIn);

        Page<BlockResponse> pageOut = requestBlockService.findAll(pageable);

        assertEquals(1, pageOut.getTotalElements());
        BlockResponse resp = pageOut.getContent().get(0);
        assertEquals(1L, resp.id());
        assertEquals(1L, resp.userId());
        assertEquals(1L, resp.cardId());
    }

    @Test
    @DisplayName("Найти все запросы по email")
    void findRequestByEmailSuccess() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<RequestBlock> pageIn = new PageImpl<>(List.of(block));

        when(userRepository.findByEmail("dmitrii@gmail.com")).thenReturn(Optional.of(user));
        when(requestBlockRepository.findByUser(user, pageable)).thenReturn(pageIn);

        Page<BlockResponse> pageOut = requestBlockService.findRequestByEmail("dmitrii@gmail.com", pageable);

        assertEquals(1, pageOut.getTotalElements());
        assertEquals(RequestStatus.NOT_STARTED, pageOut.getContent().get(0).status());
    }

    @Test
    @DisplayName("Найти все запросы по email - пользователь не найден")
    void findRequestByEmailFail() {
        when(userRepository.findByEmail("dmitrii@gmail.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> requestBlockService.findRequestByEmail("dmitrii@gmail.com", PageRequest.of(0,1))
        );
    }

    @Test
    @DisplayName("Создать запрос на блокировку")
    void createRequestBlockSuccess() {
        when(userRepository.findByEmail("dmitrii@gmail.com")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(requestBlockRepository.existsByUserAndCard(user, card)).thenReturn(false);

        requestBlockService.createRequestBlock("dmitrii@gmail.com", 1L);

        verify(requestBlockRepository, times(1)).save(argThat(rb ->
                rb.getUser() == user && rb.getCard() == card &&
                        rb.getStatus() == RequestStatus.NOT_STARTED
        ));
    }

    @Test
    @DisplayName("Создать запрос на блокировку - пользователь не найден")
    void createRequestBlockFailUser() {
        when(userRepository.findByEmail("dmitrii@gmail.com")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> requestBlockService.createRequestBlock("dmitrii@gmail.com", 1L)
        );
    }

    @Test
    @DisplayName("Создать запрос на блокировку - карта не найдена")
    void createRequestBlockFailCard() {
        when(userRepository.findByEmail("dmitrii@gmail.com")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> requestBlockService.createRequestBlock("dmitrii@gmail.com", 1L)
        );
    }

    @Test
    @DisplayName("Создать запрос на блокировку - не вляделец карты")
    void createRequestBlockFailOwner() {
        User other = new User();
        other.setId(2L);
        card.setUser(other);

        when(userRepository.findByEmail("dmitrii@gmail.com")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardOperationException.class,
                () -> requestBlockService.createRequestBlock("dmitrii@gmail.com", 1L)
        );
    }

    @Test
    @DisplayName("Создать запрос на блокировку - уже заблокирована")
    void createRequestBlockFailBlocked() {
        card.setStatus(CardStatus.BLOCKED);

        when(userRepository.findByEmail("dmitrii@gmail.com")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(CardOperationException.class,
                () -> requestBlockService.createRequestBlock("dmitrii@gmail.com", 1L)
        );
    }

    @Test
    @DisplayName("Создать запрос на блокировку - уже существует")
    void createRequestBlockFailExists() {
        when(userRepository.findByEmail("dmitrii@gmail.com")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(requestBlockRepository.existsByUserAndCard(user, card)).thenReturn(true);

        assertThrows(CardOperationException.class,
                () -> requestBlockService.createRequestBlock("dmitrii@gmail.com", 1L)
        );
    }

    @Test
    @DisplayName("Выполнить запрос на блокировку")
    void completeRequestBlockSuccess() {
        when(requestBlockRepository.findById(1L)).thenReturn(Optional.of(block));

        requestBlockService.completeRequestBlock(1L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        assertEquals(RequestStatus.COMPLETED, block.getStatus());
        verify(cardRepository).save(card);
        verify(requestBlockRepository).save(block);
    }

    @Test
    @DisplayName("Выполнить запрос на блокировку - не найден")
    void completeRequestBlockFail() {
        when(requestBlockRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> requestBlockService.completeRequestBlock(1L)
        );
    }
}
