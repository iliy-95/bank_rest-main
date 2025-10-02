package com.example.bankcards.controller.user;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.TransactionResponse;
import com.example.bankcards.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/cards/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Операции с переводами между картами пользователя")
public class TransactionController {

  private final TransactionService transactionService;

  @Operation(
      summary = "Перевод средств между своими картами",
      description = "Пользователь с ролью USER может перевести средства между своими картами."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Транзакция успешно создана",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = TransactionResponse.class))),
      @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
      @ApiResponse(responseCode = "403", description = "Доступ запрещён"),
      @ApiResponse(responseCode = "404", description = "Одна из карт не найдена"),
      @ApiResponse(responseCode = "409", description = "Недостаточно средств для перевода")
  })
  @PostMapping("/transfer")
  @PreAuthorize("hasRole('USER')")
  @Tag(name = "User")
  public ResponseEntity<TransactionResponse> transfer(@RequestBody @Valid TransferRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(transactionService.transfer(request, userDetails.getUsername()));
  }
}
