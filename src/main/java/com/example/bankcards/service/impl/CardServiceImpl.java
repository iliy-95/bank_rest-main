package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.BalanceResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.enums.CardStatusEnum;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardStatusRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardCryptoTinkService;
import com.example.bankcards.util.CardMaskService;
import com.example.bankcards.util.CardNumberGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class CardServiceImpl implements CardService {

  private final CardRepository cardRepository;

  private final UserRepository userRepository;

  private final CardStatusRepository cardStatusRepository;

  private final CardNumberGenerator cardNumberGenerator;

  private final CardCryptoTinkService cardCryptoTinkService;

  private final CardMaskService cardMaskService;

  @Override
  @Transactional
  public CreateCardResponse createCard(CreateCardRequest request) {
    var user = userRepository.findById(request.userId())
        .orElseThrow(
            () -> new NotFoundException("Пользователь с id " + request.userId() + " не найден"));

    CardStatus status = cardStatusRepository.getCardStatusByName(CardStatusEnum.ACTIVE.name());

    String plainPan = cardNumberGenerator.generateMirCard();
    String encryptedPan = cardCryptoTinkService.encrypt(plainPan);
    String lastFour = plainPan.substring(plainPan.length() - 4);

    Card card = Card.builder()
        .numberEncrypted(encryptedPan)
        .lastFourDigits(lastFour)
        .holderName(user.getFullName())
        .expiryDate(LocalDate.now().plusYears(5))
        .balance(BigDecimal.valueOf(1000))
        .status(status)
        .bankUser(user)
        .build();

    cardRepository.save(card);

    String masked = cardMaskService.mask(lastFour);

    log.info("Создана карта {} для пользователя {}", masked, user.getUsername());
    return new CreateCardResponse(
        "Карта создана!",
        card.getId(),
        masked,
        card.getHolderName(),
        card.getExpiryDate(),
        card.getBalance(),
        card.getStatus().getName(),
        card.getBankUser().getId()
    );
  }

  @Override
  @Transactional
  public CardResponse changeCardStatus(UUID cardId, CardStatusEnum newStatus) {

    var card = cardRepository.findById(cardId)
        .orElseThrow(() -> new NotFoundException("Карта с id " + cardId + " не найдена"));
    log.info("Изменение статуса карты {} на {}", card.getNumberEncrypted(), newStatus.name());

    CardStatus status = cardStatusRepository.getCardStatusByName(newStatus.name());

    if (card.getStatus().equals(status)) {
      log.warn("Карта {} уже в статусе {}", card.getNumberEncrypted(), newStatus.name());
      throw new UserAlreadyExistsException("Карта уже заблокирована");
    }

    card.setStatus(status);
    cardRepository.save(card);

    String masked = cardMaskService.mask(card.getLastFourDigits());

    return new CardResponse(
        "Статус изменен!",
        card.getId(),
        masked,
        card.getHolderName(),
        card.getExpiryDate(),
        card.getBalance(),
        card.getStatus().getName(),
        card.getBankUser().getId()
    );
  }

  @Override
  @Transactional
  public MessageResponse deleteCard(UUID cardId) {

    var card = cardRepository.findById(cardId)
        .orElseThrow(() -> new NotFoundException("Карта с id " + cardId + " не найдена"));

    cardRepository.delete(card);
    log.info("Карта {} удалена", card.getNumberEncrypted());

    return new MessageResponse("Карта удалена!");
  }

  @Override
  public Optional<CardResponse> usersCards(UUID cardId) {

    var card = cardRepository.findById(cardId)
        .orElseThrow(() -> new NotFoundException("Карта с таким id" + cardId + " не найдена"));

    String masked = cardMaskService.mask(card.getLastFourDigits());

    return Optional.of(new CardResponse(
        "Карта найдена!",
        card.getId(),
        masked,
        card.getHolderName(),
        card.getExpiryDate(),
        card.getBalance(),
        card.getStatus().getName(),
        card.getBankUser().getId()
    ));
  }


  @Override
  public Page<CardResponse> getUserCards(String username, String search, Pageable pageable) {
    Page<Card> page = cardRepository.findByUsernameAndSearch(username, search, pageable);

    return page.map(card -> {

      String masked = cardMaskService.mask(card.getLastFourDigits());

      return new CardResponse(
          "",
          card.getId(),
          masked,
          card.getHolderName(),
          card.getExpiryDate(),
          card.getBalance(),
          card.getStatus().getName(),
          card.getBankUser().getId()
      );
    });
  }

  @Override
  public Page<CardResponse> getAllCards(String search, Pageable pageable) {
    Page<Card> page = cardRepository.findAllWithSearch(search, pageable);

    return page.map(card -> {

      String masked = cardMaskService.mask(card.getLastFourDigits());

      return new CardResponse(
          "",
          card.getId(),
          masked,
          card.getHolderName(),
          card.getExpiryDate(),
          card.getBalance(),
          card.getStatus().getName(),
          card.getBankUser().getId()
      );
    });
  }

  @Override
  public MessageResponse blockCard(UUID cardId, String name) {

    var card = cardRepository.findById(cardId).orElseThrow();

    if (!card.getBankUser().getUsername().equals(name)) {
      throw new AccessDeniedException("You cannot block this card");
    }
    log.info("Пользователь {} блокирует карту {}", name, card.getNumberEncrypted());

    var blockStatus = cardStatusRepository.getCardStatusByName(CardStatusEnum.BLOCKED.name());

    if (card.getStatus().equals(blockStatus)) {
      log.warn("Карта {} уже заблокирована", card.getNumberEncrypted());
      throw new UserAlreadyExistsException("Карта уже заблокирована");
    }

    card.setStatus(blockStatus);

    cardRepository.save(card);

    return new MessageResponse("Карта успешно заблокирована");
  }

  @Override
  public BalanceResponse getBalance(UUID cardId, String username) {

    var card = cardRepository.findById(cardId)
        .orElseThrow(() -> new NotFoundException("Card not found"));

    if (!card.getBankUser().getUsername().equals(username)) {
      throw new AccessDeniedException("You cannot view this card");
    }
    log.info("Пользователь {} просмотрел баланс карты {}", username, card.getNumberEncrypted());

    return new BalanceResponse(card.getId(), card.getBalance(), card.getHolderName());
  }


}
