package com.example.bankcards.controller;


import com.example.bankcards.dto.request.card.CardTransferRequest;
import com.example.bankcards.dto.response.block.BlockResponse;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.security.UserDetailsCustom;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.RequestBlockService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("api/v1/cards")
@AllArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CardUserController {

    private final CardService cardService;
    private final RequestBlockService requestBlockService;

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getCards(
            @AuthenticationPrincipal UserDetailsCustom userDetailsCustom,
            Pageable pageable
    ) {
        return ResponseEntity.ok(cardService.findCardsByEmail(userDetailsCustom.getUsername(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCardById(
            @AuthenticationPrincipal UserDetailsCustom userDetailsCustom,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(cardService.findCardByEmailAndId(userDetailsCustom.getUsername(), id));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getCardBalance(
            @AuthenticationPrincipal UserDetailsCustom userDetailsCustom,
            @PathVariable Long id
    )  {
        return ResponseEntity.ok(cardService.findCardBalance(userDetailsCustom.getUsername(), id));
    }

    @PostMapping("/{id}/block-request")
    public ResponseEntity<BlockResponse> requestBlock(
            @AuthenticationPrincipal UserDetailsCustom userDetailsCustom,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(requestBlockService.createRequestBlock(userDetailsCustom.getUser().getId(), id));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @AuthenticationPrincipal UserDetailsCustom userDetailsCustom,
            @RequestBody CardTransferRequest request
    ) {
        cardService.transferMoney(userDetailsCustom.getUsername(), request);
        return ResponseEntity.ok().build();
    }
}
