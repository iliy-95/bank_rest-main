package com.example.bankcards.controller;

import com.example.bankcards.controller.user.TransactionController;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.TransactionResponse;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

  private static final UUID TEST_FROM_CARD_ID = UUID.fromString(
      "123e4567-e89b-12d3-a456-426614174000");

  private static final UUID TEST_TO_CARD_ID = UUID.fromString(
      "223e4567-e89b-12d3-a456-426614174000");

  private static final UUID TEST_TRANSACTION_ID = UUID.fromString(
      "323e4567-e89b-12d3-a456-426614174000");

  private static final String TEST_USERNAME = "testuser";

  private static final String TEST_PASSWORD = "password";

  private static final BigDecimal TEST_AMOUNT = new BigDecimal("1000.50");

  private static final String TEST_SUCCESS_MESSAGE = "Transfer completed successfully";

  private static final String ROLE_USER = "ROLE_USER";

  @Mock
  private TransactionService transactionService;

  @InjectMocks
  private TransactionController transactionController;

  private UserDetails userDetails;

  private TransferRequest transferRequest;

  private TransactionResponse transactionResponse;

  @BeforeEach
  void setUp() {
    userDetails = User.withUsername(TEST_USERNAME)
        .password(TEST_PASSWORD)
        .authorities(Collections.singletonList(new SimpleGrantedAuthority(ROLE_USER)))
        .build();

    transferRequest = new TransferRequest(
        TEST_FROM_CARD_ID,
        TEST_TO_CARD_ID,
        TEST_AMOUNT
    );

    transactionResponse = new TransactionResponse(
        TEST_TRANSACTION_ID,
        TransactionStatus.SUCCESS,
        TEST_SUCCESS_MESSAGE
    );
  }

  @Test
  @DisplayName("Должен успешно выполнить перевод средств между картами")
  void shouldSuccessfullyTransferFundsBetweenCards() {
    when(transactionService.transfer(eq(transferRequest), eq(TEST_USERNAME)))
        .thenReturn(transactionResponse);

    ResponseEntity<TransactionResponse> response = transactionController.transfer(transferRequest,
        userDetails);

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(TEST_TRANSACTION_ID, response.getBody().transactionId());
    assertEquals(com.example.bankcards.entity.enums.TransactionStatus.SUCCESS,
        response.getBody().status());
    assertEquals(TEST_SUCCESS_MESSAGE, response.getBody().message());
    verify(transactionService).transfer(transferRequest, TEST_USERNAME);
  }

  @Test
  @DisplayName("Должен корректно передавать имя пользователя в сервис")
  void shouldCorrectlyPassUsernameToService() {
    when(transactionService.transfer(eq(transferRequest), eq(TEST_USERNAME)))
        .thenReturn(transactionResponse);

    ResponseEntity<TransactionResponse> response = transactionController.transfer(transferRequest,
        userDetails);

    assertNotNull(response);
    verify(transactionService).transfer(transferRequest, TEST_USERNAME);
  }

  @Test
  @DisplayName("Должен возвращать статус 201 CREATED при успешном переводе")
  void shouldReturnCreatedStatusOnSuccessfulTransfer() {
    when(transactionService.transfer(any(TransferRequest.class), any(String.class)))
        .thenReturn(transactionResponse);

    ResponseEntity<TransactionResponse> response = transactionController.transfer(transferRequest,
        userDetails);

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  @DisplayName("Должен корректно создавать TransferRequest со всеми полями")
  void shouldCorrectlyCreateTransferRequestWithAllFields() {
    TransferRequest testTransferRequest = new TransferRequest(
        TEST_FROM_CARD_ID,
        TEST_TO_CARD_ID,
        TEST_AMOUNT
    );

    assertEquals(TEST_FROM_CARD_ID, testTransferRequest.fromCardId());
    assertEquals(TEST_TO_CARD_ID, testTransferRequest.toCardId());
    assertEquals(TEST_AMOUNT, testTransferRequest.amount());
  }

  @Test
  @DisplayName("Должен корректно создавать TransactionResponse со всеми полями")
  void shouldCorrectlyCreateTransactionResponseWithAllFields() {
    TransactionResponse testTransactionResponse = new TransactionResponse(
        TEST_TRANSACTION_ID,
        com.example.bankcards.entity.enums.TransactionStatus.SUCCESS,
        TEST_SUCCESS_MESSAGE
    );

    assertEquals(TEST_TRANSACTION_ID, testTransactionResponse.transactionId());
    assertEquals(com.example.bankcards.entity.enums.TransactionStatus.SUCCESS,
        testTransactionResponse.status());
    assertEquals(TEST_SUCCESS_MESSAGE, testTransactionResponse.message());
  }
}