package com.example.bankcards.controller;


import com.example.bankcards.dto.request.card.CardTransferRequest;
import com.example.bankcards.dto.response.block.BlockResponse;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.RequestBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("api/v1/cards")
@Tag(name = "Card API for USER", description = "Работа с картами пользователя. Для USER")
@AllArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CardUserController {

    private final CardService cardService;
    private final RequestBlockService requestBlockService;

    @Operation(
            summary = "Получение карт",
            description = "Возвращает все карты текущего пользователя"
    )
    @ApiResponse(responseCode = "200",
            description = "Карты получены",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CardResponse.class)
            ))
    @GetMapping
    public ResponseEntity<Page<CardResponse>> getCards(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable
    ) {
        return ResponseEntity.ok(cardService.findCardsByEmail(userDetails.getUsername(), pageable));
    }

    @Operation(
            summary = "Получение карты по ID",
            description = "Возвращает карту текущего пользователя по ID"
    )
    @ApiResponse(responseCode = "200",
            description = "Карта получена",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CardResponse.class)
            ))
    @ApiResponse(responseCode = "400",
            description = "Карта не принадлежит пользователю",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @ApiResponse(responseCode = "404",
            description = "Карта не найдена",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCardById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(cardService.findCardByEmailAndId(userDetails.getUsername(), id));
    }

    @Operation(
            summary = "Получение баланса карты по ID",
            description = "Возвращает баланс карты текущего пользователя по ID"
    )
    @ApiResponse(responseCode = "200",
            description = "Баланс карты получена",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BigDecimal.class)
            ))
    @ApiResponse(responseCode = "400",
            description = "Карта не принадлежит пользователю",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @ApiResponse(responseCode = "404",
            description = "Карта не найдена",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getCardBalance(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    )  {
        return ResponseEntity.ok(cardService.findCardBalance(userDetails.getUsername(), id));
    }

    @Operation(
            summary = "Получение запросов на блокировку",
            description = "Возвращает все запросы на блокировку текущего пользователя"
    )
    @ApiResponse(responseCode = "200",
            description = "Запросы получены",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BlockResponse.class)
            ))
    @ApiResponse(responseCode = "404",
            description = "Пользователь не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @GetMapping("/request_block")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<BlockResponse>> getRequestBlock(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable
    ) {
        return ResponseEntity.ok(requestBlockService.findRequestByEmail(userDetails.getUsername(), pageable));
    }

    @Operation(
            summary = "Создание запроса на блокировку",
            description = "Создает запрос на блокировку карты текущего пользователя и возвращает его"
    )
    @ApiResponse(responseCode = "200",
            description = "Запрос создан",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BlockResponse.class)
            ))
    @ApiResponse(responseCode = "400",
            description = "Ошибка при работе с картой",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @ApiResponse(responseCode = "404",
            description = "Карта не найдена",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @PostMapping("/{id}/request_block")
    public ResponseEntity<BlockResponse> createRequestBlock(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(requestBlockService.createRequestBlock(userDetails.getUsername(), id));
    }

    @Operation(
            summary = "Перевод между картами",
            description = "Переводит деньги между картами текущего пользователя"
    )
    @ApiResponse(responseCode = "200",
            description = "Перевод выполнен")
    @ApiResponse(responseCode = "400",
            description = "Ошибка при работе с картой",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @ApiResponse(responseCode = "404",
            description = "Карта не найдена",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CardTransferRequest request
    ) {
        cardService.transferMoney(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }
}
