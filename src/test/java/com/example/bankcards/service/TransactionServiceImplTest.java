package com.example.bankcards.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.TransactionResponse;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.impl.TransactionServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

  private static final UUID FROM_CARD_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  private static final UUID TO_CARD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private static final String USERNAME = "testuser";

  private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(1000);

  private static final BigDecimal TRANSFER_AMOUNT = BigDecimal.valueOf(500);

  private static final BigDecimal BIG_AMOUNT = BigDecimal.valueOf(2000);

  private Card fromCard;

  private Card toCard;

  @Mock
  private CardRepository cardRepository;

  @Mock
  private TransactionRepository transactionRepository;

  @InjectMocks
  private TransactionServiceImpl transactionService;

  @BeforeEach
  void setUp() {

    fromCard = new Card();
    fromCard.setId(FROM_CARD_ID);
    fromCard.setBalance(INITIAL_BALANCE);

    BankUser fromUser = new BankUser();
    fromUser.setUsername(USERNAME);
    fromCard.setBankUser(fromUser);

    toCard = new Card();
    toCard.setId(TO_CARD_ID);
    toCard.setBalance(INITIAL_BALANCE);

    BankUser toUser = new BankUser();
    toUser.setUsername(USERNAME);
    toCard.setBankUser(toUser);
  }

  @Test
  @DisplayName("Успешный перевод между своими картами")
  void testTransfer_Success() {

    when(cardRepository.findByIdWithUserAndStatus(FROM_CARD_ID)).thenReturn(Optional.of(fromCard));
    when(cardRepository.findByIdWithUserAndStatus(TO_CARD_ID)).thenReturn(Optional.of(toCard));
    when(transactionRepository.save(any(Transaction.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    TransferRequest request = new TransferRequest(FROM_CARD_ID, TO_CARD_ID, TRANSFER_AMOUNT);
    TransactionResponse response = transactionService.transfer(request, USERNAME);


    assertThat(fromCard.getBalance()).isEqualByComparingTo(
        INITIAL_BALANCE.subtract(TRANSFER_AMOUNT));
    assertThat(toCard.getBalance()).isEqualByComparingTo(INITIAL_BALANCE.add(TRANSFER_AMOUNT));
    assertThat(response.status());

    ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionRepository, times(1)).save(txCaptor.capture());
    assertThat(txCaptor.getValue().getStatus()).isEqualTo(TransactionStatus.SUCCESS);
  }

  @Test
  @DisplayName("Перевод неудачен при недостаточном балансе")
  void testTransfer_InsufficientBalance() {
    when(cardRepository.findByIdWithUserAndStatus(FROM_CARD_ID)).thenReturn(Optional.of(fromCard));
    when(cardRepository.findByIdWithUserAndStatus(TO_CARD_ID)).thenReturn(Optional.of(toCard));

    TransferRequest request = new TransferRequest(FROM_CARD_ID, TO_CARD_ID, BIG_AMOUNT);


    assertThrows(InsufficientBalanceException.class, () ->
        transactionService.transfer(request, USERNAME));

    ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionRepository).save(txCaptor.capture());
    assertThat(txCaptor.getValue().getStatus()).isEqualTo(TransactionStatus.FAILED);
  }

  @Test
  @DisplayName("Перевод неудачен, если карты не принадлежат пользователю")
  void testTransfer_AccessDenied() {
    fromCard.getBankUser().setUsername("anotherUser");
    when(cardRepository.findByIdWithUserAndStatus(FROM_CARD_ID)).thenReturn(Optional.of(fromCard));
    when(cardRepository.findByIdWithUserAndStatus(TO_CARD_ID)).thenReturn(Optional.of(toCard));

    TransferRequest request = new TransferRequest(FROM_CARD_ID, TO_CARD_ID, TRANSFER_AMOUNT);

    assertThrows(AccessDeniedException.class, () ->
        transactionService.transfer(request, USERNAME));

    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  @DisplayName("Перевод неудачен, если исходная карта не найдена")
  void testTransfer_FromCardNotFound() {
    when(cardRepository.findByIdWithUserAndStatus(FROM_CARD_ID)).thenReturn(Optional.empty());

    TransferRequest request = new TransferRequest(FROM_CARD_ID, TO_CARD_ID, TRANSFER_AMOUNT);

    assertThrows(NotFoundException.class, () ->
        transactionService.transfer(request, USERNAME));
  }

  @Test
  @DisplayName("Перевод неудачен, если карта получателя не найдена")
  void testTransfer_ToCardNotFound() {
    when(cardRepository.findByIdWithUserAndStatus(FROM_CARD_ID)).thenReturn(Optional.of(fromCard));
    when(cardRepository.findByIdWithUserAndStatus(TO_CARD_ID)).thenReturn(Optional.empty());

    TransferRequest request = new TransferRequest(FROM_CARD_ID, TO_CARD_ID, TRANSFER_AMOUNT);

    assertThrows(NotFoundException.class, () ->
        transactionService.transfer(request, USERNAME));
  }
}
