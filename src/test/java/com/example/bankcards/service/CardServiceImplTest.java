package com.example.bankcards.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.enums.CardStatusEnum;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardStatusRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.CardCryptoTinkService;
import com.example.bankcards.util.CardMaskService;
import com.example.bankcards.util.CardNumberGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  private static final UUID CARD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private static final String USERNAME = "testuser";

  private static final String FULL_NAME = "Тестовый Пользователь";

  private static final String PLAIN_PAN = "1234567890123456";

  private static final String ENCRYPTED_PAN = "encryptedPan";

  private static final String MASKED = "****3456";

  @Mock
  private CardRepository cardRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CardStatusRepository cardStatusRepository;

  @Mock
  private CardNumberGenerator cardNumberGenerator;

  @Mock
  private CardCryptoTinkService cardCryptoTinkService;

  @Mock
  private CardMaskService cardMaskService;

  @InjectMocks
  private CardServiceImpl cardService;

  private BankUser user;

  private CardStatus activeStatus;

  private Card card;

  @BeforeEach
  void setUp() {
    user = new BankUser();
    user.setId(USER_ID);
    user.setUsername(USERNAME);
    user.setFullName(FULL_NAME);

    activeStatus = new CardStatus();
    activeStatus.setName(CardStatusEnum.ACTIVE.name());

    card = Card.builder()
        .id(CARD_ID)
        .bankUser(user)
        .holderName(FULL_NAME)
        .numberEncrypted(ENCRYPTED_PAN)
        .lastFourDigits("3456")
        .balance(BigDecimal.valueOf(1000))
        .status(activeStatus)
        .expiryDate(LocalDate.now().plusYears(5))
        .build();
  }

  @Test
  @DisplayName("Успешное создание карты")
  void testCreateCard_Success() {
    CreateCardRequest request = new CreateCardRequest(USER_ID);

    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(cardStatusRepository.getCardStatusByName(CardStatusEnum.ACTIVE.name())).thenReturn(
        activeStatus);
    when(cardNumberGenerator.generateMirCard()).thenReturn(PLAIN_PAN);
    when(cardCryptoTinkService.encrypt(PLAIN_PAN)).thenReturn(ENCRYPTED_PAN);
    when(cardMaskService.mask("3456")).thenReturn(MASKED);

    CreateCardResponse response = cardService.createCard(request);

    assertThat(response).isNotNull();
    assertThat(response.numberEncrypted()).isEqualTo(MASKED);
    verify(cardRepository).save(any(Card.class));
  }

  @Test
  @DisplayName("Создание карты для несуществующего пользователя")
  void testCreateCard_UserNotFound() {
    CreateCardRequest request = new CreateCardRequest(USER_ID);
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> cardService.createCard(request));
  }

  @Test
  @DisplayName("Изменение статуса карты")
  void testChangeCardStatus_Success() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
    when(cardStatusRepository.getCardStatusByName(CardStatusEnum.BLOCKED.name())).thenReturn(
        new CardStatus());
    when(cardMaskService.mask("3456")).thenReturn(MASKED);

    CardResponse response = cardService.changeCardStatus(CARD_ID, CardStatusEnum.BLOCKED);

    assertThat(response).isNotNull();
    verify(cardRepository).save(card);
  }

  @Test
  @DisplayName("Попытка изменить статус на текущий")
  void testChangeCardStatus_AlreadySame() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
    when(cardStatusRepository.getCardStatusByName(CardStatusEnum.ACTIVE.name())).thenReturn(
        activeStatus);

    assertThrows(UserAlreadyExistsException.class,
        () -> cardService.changeCardStatus(CARD_ID, CardStatusEnum.ACTIVE));
  }

  @Test
  @DisplayName("Удаление карты")
  void testDeleteCard_Success() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

    MessageResponse response = cardService.deleteCard(CARD_ID);

    assertThat(response.message()).isEqualTo("Карта удалена!");
    verify(cardRepository).delete(card);
  }

  @Test
  @DisplayName("Удаление несуществующей карты")
  void testDeleteCard_NotFound() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> cardService.deleteCard(CARD_ID));
  }

  @Test
  @DisplayName("Получение информации о карте пользователя")
  void testUsersCards_Success() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
    when(cardMaskService.mask("3456")).thenReturn(MASKED);

    Optional<CardResponse> response = cardService.usersCards(CARD_ID);

    assertThat(response).isPresent();
    assertThat(response.get().numberEncrypted()).isEqualTo(MASKED);
  }

  @Test
  @DisplayName("Получение всех карт пользователя с пагинацией")
  void testGetUserCards() {
    Page<Card> page = new PageImpl<>(List.of(card));
    when(cardRepository.findByUsernameAndSearch(USERNAME, null, Pageable.unpaged())).thenReturn(
        page);
    when(cardMaskService.mask("3456")).thenReturn(MASKED);

    Page<CardResponse> response = cardService.getUserCards(USERNAME, null, Pageable.unpaged());

    assertThat(response.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("Получение всех карт с поиском и пагинацией")
  void testGetAllCards() {
    Page<Card> page = new PageImpl<>(List.of(card));
    when(cardRepository.findAllWithSearch(null, Pageable.unpaged())).thenReturn(page);
    when(cardMaskService.mask("3456")).thenReturn(MASKED);

    Page<CardResponse> response = cardService.getAllCards(null, Pageable.unpaged());

    assertThat(response.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("Блокировка карты пользователем")
  void testBlockCard_Success() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
    CardStatus blockedStatus = new CardStatus();
    blockedStatus.setName(CardStatusEnum.BLOCKED.name());
    when(cardStatusRepository.getCardStatusByName(CardStatusEnum.BLOCKED.name())).thenReturn(
        blockedStatus);

    MessageResponse response = cardService.blockCard(CARD_ID, USERNAME);

    assertThat(response.message()).isEqualTo("Карта успешно заблокирована");
    verify(cardRepository).save(card);
  }

  @Test
  @DisplayName("Получение баланса карты пользователем")
  void testGetBalance_Success() {
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

    var response = cardService.getBalance(CARD_ID, USERNAME);

    assertThat(response.balance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
  }

}
