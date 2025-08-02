package com.example.bankcards.controller;


import com.example.bankcards.dto.request.card.CardCreateRequest;
import com.example.bankcards.dto.response.block.BlockResponse;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.RequestBlockService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/admin/cards")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CardAdminController {

    private final CardService cardService;
    private final RequestBlockService requestBlockService;

    @GetMapping()
    public ResponseEntity<Page<CardResponse>> getAllCards(Pageable pageable) {
        Page<CardResponse> page = cardService.getAllCards(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/request_block")
    public ResponseEntity<Page<BlockResponse>> getAllRequestBlocks(Pageable pageable) {
        Page<BlockResponse> page = requestBlockService.getAll(pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            @RequestBody CardCreateRequest cardCreateRequest) {
        return ResponseEntity.ok(cardService.createCard(cardCreateRequest));
    }

    @PostMapping("/complete_request/{id}")
    public ResponseEntity<Void> completeRequestBlock(
            @PathVariable Long id) {
        requestBlockService.completeRequestBlock(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("status/{id}")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody CardStatus cardStatus
    ) {
        cardService.updateStatus(id, cardStatus);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long id
    ) {
        cardService.deleteCard(id);
        return ResponseEntity.ok().build();
    }
}
