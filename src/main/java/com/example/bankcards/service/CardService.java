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
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@AllArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserService userService;
    private final EncryptCard encryptCard;

    @Transactional(readOnly = true)
    public Page<CardResponse> findAllCards(Pageable pageable) {
        Page<Card> cards = cardRepository.findAll(pageable);

        return cards.map(this::response);
    }

    @Transactional(readOnly = true)
    public Page<CardResponse> findCardsByEmail(String email, Pageable pageable) {
        User user = userService.findByEmail(email);
        Page<Card> cards = cardRepository.findByUser(user, pageable);

        return cards.map(this::response);
    }

    @Transactional(readOnly = true)
    public CardResponse findCardByEmailAndId(String email, Long cardId) {
        User user = userService.findByEmail(email);
        Card card = findCardById(cardId);

        if (!card.getUser().equals(user)) {
            throw new CardOperationException("Вы не являетесь владельцем этой карты.");
        }

        return new CardResponse(
                card.getId(),
                numberMask(card.getNumber(), true),
                user.getFirstName(),
                user.getSecondName(),
                card.getExpirationDate(),
                card.getStatus(),
                card.getBalance()
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal findCardBalance(String email, Long cardId) {
        return findCardByEmailAndId(email, cardId).balance();
    }

    @Transactional
    public CardResponse createCard(CardCreateRequest cardCreateRequest) {
        User user = userService.findByEmail(cardCreateRequest.email());

        String cardNumber = NumberGenerator.generateNumber();
        String encryptNumber = encryptCard.encrypt(cardNumber);

        Card card = Card.builder()
                .number(encryptNumber)
                .user(user)
                .expirationDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();

        Card newCard = cardRepository.save(card);

        return new CardResponse(
                newCard.getId(),
                numberMask(cardNumber, false),
                user.getFirstName(),
                user.getSecondName(),
                newCard.getExpirationDate(),
                newCard.getStatus(),
                newCard.getBalance()
        );
    }

    @Transactional
    public void updateStatus(Long cardId, CardStatus newStatus) {
        Card card = findCardById(cardId);

        if (card.getStatus() == CardStatus.EXPIRED && newStatus == CardStatus.ACTIVE) {
            throw new CardOperationException("Невозможно активировать просроченную карту");
        }

        card.setStatus(newStatus);
        cardRepository.save(card);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        Card card = findCardById(cardId);

        cardRepository.delete(card);
    }

    @Transactional
    public void transferMoney(String email, CardTransferRequest cardTransferRequest) {
        User user = userService.findByEmail(email);
        Card fromCard = findCardById(cardTransferRequest.fromCardId());
        Card toCard = findCardById(cardTransferRequest.toCardId());

        if (cardTransferRequest.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardOperationException("Сумма должна быть положительной");
        }

        if (!fromCard.getUser().equals(user) || !toCard.getUser().equals(user)) {
            throw new CardOperationException("Перевод возможен только между вашими картами");
        }

        if (cardTransferRequest.fromCardId().equals(cardTransferRequest.toCardId())) {
            throw new CardOperationException("Нельзя переводить на ту же карту");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("Перевод возможен только между активными картами");
        }

        if (fromCard.getBalance().compareTo(cardTransferRequest.amount()) < 0) {
            throw new CardOperationException("Недостаточно средств");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(cardTransferRequest.amount()));
        toCard.setBalance(toCard.getBalance().add(cardTransferRequest.amount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    private CardResponse response(Card card) {
        String numberMasked = this.numberMask(card.getNumber(), true);

        return new CardResponse(
                card.getId(),
                numberMasked,
                card.getUser().getFirstName(),
                card.getUser().getSecondName(),
                card.getExpirationDate(),
                card.getStatus(),
                card.getBalance()
        );
    }

    private String numberMask(String number, Boolean encrypted) {
        if (encrypted) {
            try {
                number = encryptCard.decrypt(number);
            } catch (Exception e) {
                return "**** **** **** ****";
            }
        }

        return "**** **** **** " + number.substring(number.length() - 4);
    }

    private Card findCardById(Long cardId) {
        return cardRepository
                .findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта не найдена. ID: " + cardId));
    }
}
