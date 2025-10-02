package com.example.bankcards.controller;

import com.example.bankcards.controller.user.CardUserController;
import com.example.bankcards.dto.response.BalanceResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.MessageResponse;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardUserControllerTest {

  private static final UUID TEST_CARD_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  private static final UUID TEST_BANK_USER_ID = UUID.fromString(
      "223e4567-e89b-12d3-a456-426614174000");

  private static final String TEST_USERNAME = "testuser";

  private static final String TEST_PASSWORD = "password";

  private static final String TEST_SEARCH_QUERY = "1234";

  private static final BigDecimal TEST_BALANCE = new BigDecimal("1000.50");

  private static final String SUCCESS_MESSAGE = "Запрос на блокировку карты успешно отправлен";

  private static final String TEST_MESSAGE = "Test message";

  private static final String TEST_NUMBER_ENCRYPTED = "encrypted_card_number";

  private static final String TEST_HOLDER_NAME = "Test User";

  private static final LocalDate TEST_EXPIRY_DATE = LocalDate.of(2025, 12, 31);

  private static final String TEST_STATUS = "ACTIVE";

  private static final String ROLE_USER = "ROLE_USER";

  @Mock
  private CardService cardService;

  @Mock
  private Pageable pageable;

  @InjectMocks
  private CardUserController cardUserController;

  private UserDetails userDetails;

  private Authentication authentication;

  private CardResponse cardResponse;

  private BalanceResponse balanceResponse;

  private MessageResponse messageResponse;

  @BeforeEach
  void setUp() {
    userDetails = User.withUsername(TEST_USERNAME)
        .password(TEST_PASSWORD)
        .authorities(Collections.singletonList(new SimpleGrantedAuthority(ROLE_USER)))
        .build();

    authentication = new TestingAuthenticationToken(
        TEST_USERNAME,
        TEST_PASSWORD,
        Collections.singletonList(new SimpleGrantedAuthority(ROLE_USER))
    );

    cardResponse = new CardResponse(
        TEST_MESSAGE,
        TEST_CARD_ID,
        TEST_NUMBER_ENCRYPTED,
        TEST_HOLDER_NAME,
        TEST_EXPIRY_DATE,
        TEST_BALANCE,
        TEST_STATUS,
        TEST_BANK_USER_ID
    );

    balanceResponse = new BalanceResponse(TEST_CARD_ID, TEST_BALANCE, TEST_HOLDER_NAME);
    messageResponse = new MessageResponse(SUCCESS_MESSAGE);
  }

  @Test
  @DisplayName("Должен успешно отправить запрос на блокировку карты")
  void shouldSuccessfullySendBlockCardRequest() {
    when(cardService.blockCard(TEST_CARD_ID, TEST_USERNAME)).thenReturn(messageResponse);

    MessageResponse actualResponse = cardUserController.blockCard(TEST_CARD_ID, userDetails);

    assertNotNull(actualResponse);
    assertEquals(SUCCESS_MESSAGE, actualResponse.message());
    verify(cardService).blockCard(TEST_CARD_ID, TEST_USERNAME);
  }

  @Test
  @DisplayName("Должен успешно получить баланс карты")
  void shouldSuccessfullyGetCardBalance() {
    when(cardService.getBalance(TEST_CARD_ID, TEST_USERNAME)).thenReturn(balanceResponse);

    BalanceResponse actualResponse = cardUserController.getBalance(TEST_CARD_ID, userDetails);

    assertNotNull(actualResponse);
    assertEquals(TEST_CARD_ID, actualResponse.cardId());
    assertEquals(TEST_BALANCE, actualResponse.balance());
    assertEquals(TEST_HOLDER_NAME, actualResponse.holderName());
    verify(cardService).getBalance(TEST_CARD_ID, TEST_USERNAME);
  }

  @Test
  @DisplayName("Должен успешно получить список карт пользователя с поиском")
  void shouldSuccessfullyGetUserCardsWithSearch() {
    Page<CardResponse> expectedPage = new PageImpl<>(List.of(cardResponse));

    when(cardService.getUserCards(TEST_USERNAME, TEST_SEARCH_QUERY, pageable))
        .thenReturn(expectedPage);

    Page<CardResponse> actualPage = cardUserController.getMyCards(authentication, TEST_SEARCH_QUERY,
        pageable);

    assertNotNull(actualPage);
    assertEquals(1, actualPage.getTotalElements());
    assertEquals(cardResponse, actualPage.getContent().get(0));
    verify(cardService).getUserCards(TEST_USERNAME, TEST_SEARCH_QUERY, pageable);
  }

  @Test
  @DisplayName("Должен успешно получить список карт пользователя без поиска")
  void shouldSuccessfullyGetUserCardsWithoutSearch() {
    Page<CardResponse> expectedPage = new PageImpl<>(List.of(cardResponse));

    when(cardService.getUserCards(eq(TEST_USERNAME), eq(null), any(Pageable.class)))
        .thenReturn(expectedPage);

    Page<CardResponse> actualPage = cardUserController.getMyCards(authentication, null, pageable);

    assertNotNull(actualPage);
    assertEquals(1, actualPage.getTotalElements());
    assertEquals(cardResponse, actualPage.getContent().get(0));
    verify(cardService).getUserCards(TEST_USERNAME, null, pageable);
  }

  @Test
  @DisplayName("Должен успешно получить пустой список карт пользователя")
  void shouldSuccessfullyGetEmptyUserCards() {
    Page<CardResponse> expectedPage = new PageImpl<>(Collections.emptyList());

    when(cardService.getUserCards(TEST_USERNAME, TEST_SEARCH_QUERY, pageable))
        .thenReturn(expectedPage);

    Page<CardResponse> actualPage = cardUserController.getMyCards(authentication, TEST_SEARCH_QUERY,
        pageable);

    assertNotNull(actualPage);
    assertEquals(0, actualPage.getTotalElements());
    verify(cardService).getUserCards(TEST_USERNAME, TEST_SEARCH_QUERY, pageable);
  }

  @Test
  @DisplayName("Должен использовать правильное имя пользователя из аутентификации")
  void shouldUseCorrectUsernameFromAuthentication() {
    Page<CardResponse> expectedPage = new PageImpl<>(List.of(cardResponse));

    when(cardService.getUserCards(TEST_USERNAME, TEST_SEARCH_QUERY, pageable))
        .thenReturn(expectedPage);

    Page<CardResponse> actualPage = cardUserController.getMyCards(authentication, TEST_SEARCH_QUERY,
        pageable);

    assertNotNull(actualPage);
    assertEquals(TEST_USERNAME, authentication.getName());
    verify(cardService).getUserCards(TEST_USERNAME, TEST_SEARCH_QUERY, pageable);
  }

  @Test
  @DisplayName("Должен корректно создавать CardResponse со всеми полями")
  void shouldCorrectlyCreateCardResponseWithAllFields() {
    CardResponse testCardResponse = new CardResponse(
        TEST_MESSAGE,
        TEST_CARD_ID,
        TEST_NUMBER_ENCRYPTED,
        TEST_HOLDER_NAME,
        TEST_EXPIRY_DATE,
        TEST_BALANCE,
        TEST_STATUS,
        TEST_BANK_USER_ID
    );

    assertEquals(TEST_MESSAGE, testCardResponse.message());
    assertEquals(TEST_NUMBER_ENCRYPTED, testCardResponse.numberEncrypted());
    assertEquals(TEST_HOLDER_NAME, testCardResponse.holderName());
    assertEquals(TEST_EXPIRY_DATE, testCardResponse.expiryDate());
    assertEquals(TEST_BALANCE, testCardResponse.balance());
    assertEquals(TEST_STATUS, testCardResponse.status());
    assertEquals(TEST_BANK_USER_ID, testCardResponse.bankUser());
  }

  @Test
  @DisplayName("Должен корректно создавать BalanceResponse со всеми полями")
  void shouldCorrectlyCreateBalanceResponseWithAllFields() {
    BalanceResponse testBalanceResponse = new BalanceResponse(
        TEST_CARD_ID,
        TEST_BALANCE,
        TEST_HOLDER_NAME
    );

    assertEquals(TEST_CARD_ID, testBalanceResponse.cardId());
    assertEquals(TEST_BALANCE, testBalanceResponse.balance());
    assertEquals(TEST_HOLDER_NAME, testBalanceResponse.holderName());
  }
}