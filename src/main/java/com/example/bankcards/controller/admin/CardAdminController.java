package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.entity.enums.CardStatusEnum;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/admin/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tags(value = {
    @Tag(name = "Card", description = "Операции с картами"),
    @Tag(name = "Admin")
})
public class CardAdminController {

  private final CardService cardService;

  @Operation(
      summary = "Просмотр всех карт",
      description = "Администратор может просматривать все карты с возможностью поиска и пагинации."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Список всех карт получен",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = CardResponse.class)))
  })
  @GetMapping("/all")
  @Tag(name = "Admin")
  public Page<CardResponse> getAllCards(
      @RequestParam(required = false) String search,
      @ParameterObject Pageable pageable
  ) {

    return cardService.getAllCards(search, pageable);
  }

  @Operation(
      summary = "Просмотр информации о карте по ID",
      description = "Администратор может просмотреть карту пользователя по ID карты."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Информация о карте успешно получена",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = CardResponse.class))),
      @ApiResponse(responseCode = "404", description = "Карта с таким ID не найдена")
  })
  @GetMapping("/{cardId}/users_cards")
  @Tag(name = "Admin")
  public ResponseEntity<Optional<CardResponse>> usersCards(@PathVariable UUID cardId) {

    return ResponseEntity.ok(cardService.usersCards(cardId));
  }

  @Operation(
      summary = "Создание карты пользователю",
      description = "Администратор может создать карту пользователю."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Карта успешно создана",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = CreateCardResponse.class))),
      @ApiResponse(responseCode = "404", description = "Пользователь с таким ID не найден")
  })
  @PostMapping
  @Tag(name = "Admin")
  public ResponseEntity<CreateCardResponse> createCard(@RequestBody @Valid CreateCardRequest request) {

    return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(request));
  }

  @Operation(
      summary = "Активация карты",
      description = "Администратор активирует карту пользователю."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Карта успешно активирована",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = CardResponse.class))),
      @ApiResponse(responseCode = "404", description = "Карта с таким ID не найдена")
  })
  @PatchMapping("/{cardId}/activate")
  @Tag(name = "Admin")
  public ResponseEntity<CardResponse> activateCard(@PathVariable UUID cardId) {

    return ResponseEntity.ok(cardService.changeCardStatus(cardId, CardStatusEnum.ACTIVE));
  }

  @Operation(
      summary = "Блокировка карты",
      description = "Администратор блокирует карту пользователю."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = CardResponse.class))),
      @ApiResponse(responseCode = "404", description = "Карта с таким ID не найдена")
  })
  @PatchMapping("/{cardId}/block")
  @Tag(name = "Admin")
  public ResponseEntity<CardResponse> blockCard(@PathVariable UUID cardId) {

    return ResponseEntity.ok(cardService.changeCardStatus(cardId, CardStatusEnum.BLOCKED));
  }

  @Operation(
      summary = "Пометка карты как просроченной",
      description = "Администратор переводит карту в статус 'просрочена'."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Карта успешно помечена как просроченная",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = CardResponse.class))),
      @ApiResponse(responseCode = "404", description = "Карта с таким ID не найдена")
  })
  @PatchMapping("/{cardId}/expired")
  @Tag(name = "Admin")
  public ResponseEntity<CardResponse> expiredCard(@PathVariable UUID cardId) {

    return ResponseEntity.ok(cardService.changeCardStatus(cardId, CardStatusEnum.EXPIRED));
  }

  @Operation(
      summary = "Удаление карты",
      description = "Администратор удаляет карту пользователя."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Карта успешно удалена",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = MessageResponse.class))),
      @ApiResponse(responseCode = "404", description = "Карта с таким ID не найдена")
  })
  @DeleteMapping("/{cardId}/delete")
  @Tag(name = "Admin")
  public ResponseEntity<MessageResponse> deleteCard(@PathVariable UUID cardId) {

    return ResponseEntity.ok(cardService.deleteCard(cardId));
  }

}

