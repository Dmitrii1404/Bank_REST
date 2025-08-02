package com.example.bankcards.controller;


import com.example.bankcards.dto.request.card.CardCreateRequest;
import com.example.bankcards.dto.response.block.BlockResponse;
import com.example.bankcards.dto.response.card.CardResponse;
import com.example.bankcards.entity.card.CardStatus;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/admin/cards")
@Tag(name = "Card API for ADMIN", description = "Работа с картами пользователя. Для ADMIN")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CardAdminController {

    private final CardService cardService;
    private final RequestBlockService requestBlockService;

    @Operation(
            summary = "Получение всех карт",
            description = "Возвращает карты всех пользователей"
    )
    @ApiResponse(responseCode = "200",
            description = "Карты получены",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CardResponse.class)
            ))
    @GetMapping()
    public ResponseEntity<Page<CardResponse>> getAllCards(Pageable pageable) {
        Page<CardResponse> page = cardService.findAllCards(pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "Всех запросов на блокировку карт",
            description = "Возвращает все запросы на блокировку карт"
    )
    @ApiResponse(responseCode = "200",
            description = "Запросы получены",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BlockResponse.class)
            ))
    @GetMapping("/request_block")
    public ResponseEntity<Page<BlockResponse>> getAllRequestBlocks(Pageable pageable) {
        Page<BlockResponse> page = requestBlockService.findAll(pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "Создание карты",
            description = "Создает карту указанному пользователю"
    )
    @ApiResponse(responseCode = "200",
            description = "Карта создана",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CardResponse.class)
            ))
    @ApiResponse(responseCode = "404",
            description = "Пользователь не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            @Valid @RequestBody CardCreateRequest cardCreateRequest) {
        return ResponseEntity.ok(cardService.createCard(cardCreateRequest));
    }

    @Operation(
            summary = "Выполнить запрос",
            description = "Выполняет указанный запрос на блокировку карты"
    )
    @ApiResponse(responseCode = "200",
            description = "Запрос выполнен")
    @ApiResponse(responseCode = "404",
            description = "Запрос не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @PostMapping("/complete_request/{id}")
    public ResponseEntity<Void> completeRequestBlock(
            @PathVariable Long id) {
        requestBlockService.completeRequestBlock(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Изменить статус карты",
            description = "Изменяет статус выбранной карты"
    )
    @ApiResponse(responseCode = "200",
            description = "Статус изменен")
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
    @PutMapping("status/{id}")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CardStatus cardStatus
    ) {
        cardService.updateStatus(id, cardStatus);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Удаление карты",
            description = "Удаляет указанную карту"
    )
    @ApiResponse(responseCode = "200",
            description = "Карта удалена")
    @ApiResponse(responseCode = "404",
            description = "Карта не найдена",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string")
            ))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long id
    ) {
        cardService.deleteCard(id);
        return ResponseEntity.ok().build();
    }
}
