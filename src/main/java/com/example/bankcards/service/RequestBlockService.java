package com.example.bankcards.service;


import com.example.bankcards.dto.response.block.BlockResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.request.RequestBlock;
import com.example.bankcards.entity.request.RequestStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RequestBlockRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class RequestBlockService {

    private final RequestBlockRepository requestBlockRepository;
    private final CardRepository cardRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<BlockResponse> findAll(Pageable pageable) {
        Page<RequestBlock> requestBlocks = requestBlockRepository.findAll(pageable);

        return requestBlocks.map(this::response);
    }

    @Transactional(readOnly = true)
    public Page<BlockResponse> findRequestByEmail(String email, Pageable pageable) {
        User user = userService.findByEmail(email);

        Page<RequestBlock> requestBlocks = requestBlockRepository.findByUser(user, pageable);

        return requestBlocks.map(this::response);
    }

    @Transactional
    public BlockResponse createRequestBlock(String email, Long cardId) {
        User user = userService.findByEmail(email);
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new NotFoundException("Карта не найдена. Id: " + cardId));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new CardOperationException("Карта вам не принадлежит");
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardOperationException("Карта была заблокирована ранее");
        }

        User userCard = card.getUser();
        boolean exists = requestBlockRepository.existsByUserAndCard(userCard, card);

        if (exists) {
            throw new CardOperationException("Запрос на блокировку выбранной карты уже существует");
        }

        RequestBlock requestBlock = new RequestBlock();
        requestBlock.setUser(user);
        requestBlock.setCard(card);
        requestBlockRepository.save(requestBlock);

        return response(requestBlock);
    }

    @Transactional
    public void completeRequestBlock(Long requestId) {
        RequestBlock requestBlock = requestBlockRepository
                .findById(requestId)
                .orElseThrow(() -> new NotFoundException("Не найден запрос на блокировку"));

        Card card = requestBlock.getCard();
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        requestBlock.setStatus(RequestStatus.COMPLETED);
        requestBlockRepository.save(requestBlock);
    }

    private BlockResponse response(RequestBlock requestBlock) {
        return new BlockResponse(
                requestBlock.getId(),
                requestBlock.getUser().getId(),
                requestBlock.getCard().getId(),
                requestBlock.getStatus(),
                requestBlock.getRequestedAt()
        );
    }

}
