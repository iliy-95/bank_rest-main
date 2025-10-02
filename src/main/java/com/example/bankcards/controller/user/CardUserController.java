package com.example.bankcards.controller.user;

import com.example.bankcards.dto.response.BalanceResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/cards")
@PreAuthorize("hasRole('USER')")

@Tags(value = {
    @Tag(name = "Card", description = "Операции с картами"),
    @Tag(name = "User")
})
public class CardUserController {

  private final CardService cardService;

  @Operation(
      summary = "Запрос на блокировку своей карты",
      description = "Пользователь с ролью USER может отправить запрос на блокировку своей карты."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Запрос на блокировку карты успешно отправлен",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = MessageResponse.class))),
      @ApiResponse(responseCode = "403", description = "Доступ запрещён"),
      @ApiResponse(responseCode = "409", description = "Карта уже заблокирована")
  })
  @PatchMapping("/req_block/{cardId}")
  @PreAuthorize("hasRole('USER')")
  @Tag(name = "User")
  public MessageResponse blockCard(@PathVariable UUID cardId,
      @AuthenticationPrincipal UserDetails userDetails) {

    return cardService.blockCard(cardId, userDetails.getUsername());
  }

  @Operation(
      summary = "Просмотр баланса карты",
      description = "Пользователь с ролью USER может посмотреть баланс своей карты."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Баланс успешно получен",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = BalanceResponse.class))),
      @ApiResponse(responseCode = "403", description = "Доступ запрещён"),
      @ApiResponse(responseCode = "404", description = "Карта с таким ID не найдена")
  })
  @GetMapping("/{cardId}/balance")
  @PreAuthorize("hasRole('USER')")
  @Tag(name = "User")
  public BalanceResponse getBalance(@PathVariable UUID cardId,
      @AuthenticationPrincipal UserDetails userDetails) {

    return cardService.getBalance(cardId, userDetails.getUsername());
  }

  @Operation(
      summary = "Просмотр своих карт (поиск + пагинация)",
      description = "Пользователь с ролью USER может просмотреть список своих карт с возможностью поиска и пагинации."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Список карт получен",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = CardResponse.class)))
  })
  @GetMapping("/my_cards")
  @PreAuthorize("hasRole('USER')")
  @Tag(name = "User")
  public Page<CardResponse> getMyCards(
      Authentication authentication,
      @RequestParam(required = false) String search,
      @ParameterObject Pageable pageable
  ) {

    return cardService.getUserCards(authentication.getName(), search, pageable);
  }



}
