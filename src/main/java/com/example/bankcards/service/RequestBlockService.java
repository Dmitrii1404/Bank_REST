package com.example.bankcards.service;


import com.example.bankcards.dto.response.block.BlockResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.request.RequestBlock;
import com.example.bankcards.entity.request.RequestStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.OperationException;
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
    private final UserRepository userRepository;

    public Page<BlockResponse> getAll(Pageable pageable) {
        Page<RequestBlock> requestBlocks = requestBlockRepository.findAll(pageable);

        return requestBlocks.map(this::response);
    }

    public Page<BlockResponse> getRequestByUserId(Long id, Pageable pageable) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден. Id: " + id));

        Page<RequestBlock> requestBlocks = requestBlockRepository.findByUser(user, pageable);

        return requestBlocks.map(this::response);
    }

    public BlockResponse createRequestBlock(Long userId, Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new NotFoundException("Карта не найдена. Id: " + cardId));

        if (!card.getUser().getId().equals(userId)) {
            throw new OperationException("Карта вам не принадлежит");
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new OperationException("Карта была заблокирована ранее");
        }

        User user = card.getUser();
        boolean exists = requestBlockRepository.existsByUserAndCard(user, card);

        if (exists) {
            throw new OperationException("Запрос на блокировку выбранной карты уже существует");
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
