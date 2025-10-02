package com.example.bankcards.controller;

import com.example.bankcards.controller.admin.CardAdminController;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.entity.enums.CardStatusEnum;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardAdminControllerTest {

  private static final UUID TEST_CARD_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  private static final UUID TEST_USER_ID = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");

  private static final String TEST_SEARCH_QUERY = "1234";

  private static final String TEST_HOLDER_NAME = "Test User";

  private static final String TEST_MESSAGE = "Test message";

  private static final String TEST_NUMBER_ENCRYPTED = "encrypted_card_number";

  private static final LocalDate TEST_EXPIRY_DATE = LocalDate.of(2025, 12, 31);

  private static final BigDecimal TEST_BALANCE = new BigDecimal("1000.50");

  private static final String TEST_STATUS = "ACTIVE";

  private static final String SUCCESS_MESSAGE = "Operation completed successfully";

  @Mock
  private CardService cardService;

  @Mock
  private Pageable pageable;

  @InjectMocks
  private CardAdminController cardAdminController;

  private CardResponse cardResponse;

  private CreateCardResponse createCardResponse;

  private MessageResponse messageResponse;

  private CreateCardRequest createCardRequest;

  @BeforeEach
  void setUp() {
    cardResponse = new CardResponse(
        TEST_MESSAGE,
        TEST_CARD_ID,
        TEST_NUMBER_ENCRYPTED,
        TEST_HOLDER_NAME,
        TEST_EXPIRY_DATE,
        TEST_BALANCE,
        TEST_STATUS,
        TEST_USER_ID
    );

    createCardResponse = new CreateCardResponse(
        TEST_MESSAGE,
        TEST_CARD_ID,
        TEST_NUMBER_ENCRYPTED,
        TEST_HOLDER_NAME,
        TEST_EXPIRY_DATE,
        TEST_BALANCE,
        TEST_STATUS,
        TEST_USER_ID
    );

    messageResponse = new MessageResponse(SUCCESS_MESSAGE);

    createCardRequest = new CreateCardRequest(TEST_USER_ID);
  }

  @Test
  @DisplayName("Должен успешно получить все карты с поиском")
  void shouldSuccessfullyGetAllCardsWithSearch() {
    Page<CardResponse> expectedPage = new PageImpl<>(List.of(cardResponse));
    when(cardService.getAllCards(TEST_SEARCH_QUERY, pageable)).thenReturn(expectedPage);

    Page<CardResponse> actualPage = cardAdminController.getAllCards(TEST_SEARCH_QUERY, pageable);

    assertNotNull(actualPage);
    assertEquals(1, actualPage.getTotalElements());
    assertEquals(cardResponse, actualPage.getContent().get(0));
    verify(cardService).getAllCards(TEST_SEARCH_QUERY, pageable);
  }

  @Test
  @DisplayName("Должен успешно получить все карты без поиска")
  void shouldSuccessfullyGetAllCardsWithoutSearch() {
    Page<CardResponse> expectedPage = new PageImpl<>(List.of(cardResponse));
    when(cardService.getAllCards(null, pageable)).thenReturn(expectedPage);

    Page<CardResponse> actualPage = cardAdminController.getAllCards(null, pageable);

    assertNotNull(actualPage);
    assertEquals(1, actualPage.getTotalElements());
    assertEquals(cardResponse, actualPage.getContent().get(0));
    verify(cardService).getAllCards(null, pageable);
  }

  @Test
  @DisplayName("Должен успешно получить пустой список карт")
  void shouldSuccessfullyGetEmptyCardsList() {
    Page<CardResponse> expectedPage = new PageImpl<>(Collections.emptyList());
    when(cardService.getAllCards(TEST_SEARCH_QUERY, pageable)).thenReturn(expectedPage);

    Page<CardResponse> actualPage = cardAdminController.getAllCards(TEST_SEARCH_QUERY, pageable);

    assertNotNull(actualPage);
    assertEquals(0, actualPage.getTotalElements());
    verify(cardService).getAllCards(TEST_SEARCH_QUERY, pageable);
  }

  @Test
  @DisplayName("Должен успешно получить информацию о карте по ID")
  void shouldSuccessfullyGetCardById() {
    Optional<CardResponse> expectedResponse = Optional.of(cardResponse);
    when(cardService.usersCards(TEST_CARD_ID)).thenReturn(expectedResponse);

    ResponseEntity<Optional<CardResponse>> response = cardAdminController.usersCards(TEST_CARD_ID);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(cardResponse, response.getBody().get());
    verify(cardService).usersCards(TEST_CARD_ID);
  }

  @Test
  @DisplayName("Должен успешно создать карту")
  void shouldSuccessfullyCreateCard() {
    when(cardService.createCard(createCardRequest)).thenReturn(createCardResponse);

    ResponseEntity<CreateCardResponse> response = cardAdminController.createCard(createCardRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(TEST_MESSAGE, response.getBody().message());
    assertEquals(TEST_NUMBER_ENCRYPTED, response.getBody().numberEncrypted());
    assertEquals(TEST_HOLDER_NAME, response.getBody().holderName());
    verify(cardService).createCard(createCardRequest);
  }

  @Test
  @DisplayName("Должен успешно активировать карту")
  void shouldSuccessfullyActivateCard() {
    when(cardService.changeCardStatus(TEST_CARD_ID, CardStatusEnum.ACTIVE)).thenReturn(
        cardResponse);

    ResponseEntity<CardResponse> response = cardAdminController.activateCard(TEST_CARD_ID);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(cardResponse, response.getBody());
    verify(cardService).changeCardStatus(TEST_CARD_ID, CardStatusEnum.ACTIVE);
  }

  @Test
  @DisplayName("Должен успешно заблокировать карту")
  void shouldSuccessfullyBlockCard() {
    when(cardService.changeCardStatus(TEST_CARD_ID, CardStatusEnum.BLOCKED)).thenReturn(
        cardResponse);

    ResponseEntity<CardResponse> response = cardAdminController.blockCard(TEST_CARD_ID);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(cardResponse, response.getBody());
    verify(cardService).changeCardStatus(TEST_CARD_ID, CardStatusEnum.BLOCKED);
  }

  @Test
  @DisplayName("Должен успешно пометить карту как просроченную")
  void shouldSuccessfullyMarkCardAsExpired() {
    when(cardService.changeCardStatus(TEST_CARD_ID, CardStatusEnum.EXPIRED)).thenReturn(
        cardResponse);

    ResponseEntity<CardResponse> response = cardAdminController.expiredCard(TEST_CARD_ID);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(cardResponse, response.getBody());
    verify(cardService).changeCardStatus(TEST_CARD_ID, CardStatusEnum.EXPIRED);
  }

  @Test
  @DisplayName("Должен успешно удалить карту")
  void shouldSuccessfullyDeleteCard() {
    when(cardService.deleteCard(TEST_CARD_ID)).thenReturn(messageResponse);

    ResponseEntity<MessageResponse> response = cardAdminController.deleteCard(TEST_CARD_ID);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(SUCCESS_MESSAGE, response.getBody().message());
    verify(cardService).deleteCard(TEST_CARD_ID);
  }

  @Test
  @DisplayName("Должен корректно создавать CardResponse со всеми полями")
  void shouldCorrectlyCreateCardResponseWithAllFields() {
    cardResponse = new CardResponse(
        TEST_MESSAGE,
        TEST_CARD_ID,
        TEST_NUMBER_ENCRYPTED,
        TEST_HOLDER_NAME,
        TEST_EXPIRY_DATE,
        TEST_BALANCE,
        TEST_STATUS,
        TEST_USER_ID
    );

    assertEquals(TEST_MESSAGE, cardResponse.message());
    assertEquals(TEST_CARD_ID, cardResponse.id());
    assertEquals(TEST_NUMBER_ENCRYPTED, cardResponse.numberEncrypted());
    assertEquals(TEST_HOLDER_NAME, cardResponse.holderName());
    assertEquals(TEST_EXPIRY_DATE, cardResponse.expiryDate());
    assertEquals(TEST_BALANCE, cardResponse.balance());
    assertEquals(TEST_STATUS, cardResponse.status());
    assertEquals(TEST_USER_ID, cardResponse.bankUser());
  }

  @Test
  @DisplayName("Должен корректно создавать CreateCardRequest со всеми полями")
  void shouldCorrectlyCreateCreateCardRequestWithAllFields() {
    CreateCardRequest testRequest = new CreateCardRequest(TEST_USER_ID);

    assertEquals(TEST_USER_ID, testRequest.userId());
  }

  @Test
  @DisplayName("Должен корректно создавать CreateCardResponse со всеми полями")
  void shouldCorrectlyCreateCreateCardResponseWithAllFields() {
    CreateCardResponse testResponse = new CreateCardResponse(
        TEST_MESSAGE,
        TEST_CARD_ID,
        TEST_NUMBER_ENCRYPTED,
        TEST_HOLDER_NAME,
        TEST_EXPIRY_DATE,
        TEST_BALANCE,
        TEST_STATUS,
        TEST_USER_ID
    );

    assertEquals(TEST_MESSAGE, testResponse.message());
    assertEquals(TEST_NUMBER_ENCRYPTED, testResponse.numberEncrypted());
    assertEquals(TEST_HOLDER_NAME, testResponse.holderName());
    assertEquals(TEST_EXPIRY_DATE, testResponse.expiryDate());
    assertEquals(TEST_BALANCE, testResponse.balance());
    assertEquals(TEST_STATUS, testResponse.status());
    assertEquals(TEST_USER_ID, testResponse.bankUser());
  }
}