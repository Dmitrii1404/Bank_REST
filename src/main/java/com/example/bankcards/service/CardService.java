package com.example.bankcards.service;


import com.example.bankcards.dto.request.card.CardCreateRequest;
import com.example.bankcards.dto.request.card.CardTransferRequest;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.OperationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final EncryptCard encryptCard;

    public Page<CardResponse> getAllCards(Pageable pageable) {
        Page<Card> cards = cardRepository.findAll(pageable);

        return cards.map(this::response);
    }

    public Page<CardResponse> getCardsByEmail(String email, Pageable pageable) {
        User user = getUserByEmail(email);

        Page<Card> cards = cardRepository.findByUser(user, pageable);

        return cards.map(this::response);
    }

    public CardResponse getCardByIdAndEmail(String email, Long cardId) {
        User user = getUserByEmail(email);

        Card card = getCardById(cardId);

        if (!card.getUser().equals(user)) {
            throw new OperationException("Вы не являетесь владельцем этой карты.");
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

    public BigDecimal getCardBalance(String email, Long cardId) {
        return getCardByIdAndEmail(email, cardId).balance();
    }

    public CardResponse createCard(CardCreateRequest cardCreateRequest) {
        User user = getUserByEmail(cardCreateRequest.email());

        String cardNumber = NumberGenerator.generateNumber();
        String encryptNumber = encryptCard.encrypt(cardNumber);

        Card card = Card.builder()
                .number(encryptNumber)
                .user(user)
                .expirationDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
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

    public void updateStatus(Long cardId, CardStatus newStatus) {
        Card card = getCardById(cardId);

        if (card.getStatus() == CardStatus.EXPIRED && newStatus == CardStatus.ACTIVE) {
            throw new OperationException("Невозможно активировать просроченную карту");
        }

        card.setStatus(newStatus);
        cardRepository.save(card);
    }

    public void deleteCard(Long cardId) {
        Card card = getCardById(cardId);

        cardRepository.delete(card);
    }

    @Transactional
    public void transferMoney(String email, CardTransferRequest cardTransferRequest) {
        User user = getUserByEmail(email);
        Card fromCard = getCardById(cardTransferRequest.fromCardId());
        Card toCard = getCardById(cardTransferRequest.toCardId());

        if (!fromCard.getUser().equals(user) || !toCard.getUser().equals(user)) {
            throw new OperationException("Перевод возможен только между вашими картами");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new OperationException("Перевод возможен только между активными картами");
        }

        if (fromCard.getBalance().compareTo(toCard.getBalance()) < 0) {
            throw new OperationException("Недостаточно средств");
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

    private Card getCardById(Long cardId) {
        return cardRepository
                .findById(cardId)
                .orElseThrow(() -> new NotFoundException("Карта не найдена. ID: " + cardId));
    }

    private User getUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден. Email: " + email));
    }

}
