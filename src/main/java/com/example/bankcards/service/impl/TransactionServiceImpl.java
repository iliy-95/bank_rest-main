package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.TransactionResponse;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

  private final CardRepository cardRepository;

  private final TransactionRepository transactionRepository;

  @Override
  @Transactional
  public TransactionResponse transfer(TransferRequest request, String username) {

    var fromCard = cardRepository.findByIdWithUserAndStatus(request.fromCardId())
        .orElseThrow(() -> new NotFoundException("Исходная карта с id " + request.fromCardId() + " не найдена"));

    var toCard = cardRepository.findByIdWithUserAndStatus(request.toCardId())
        .orElseThrow(() -> new NotFoundException("Карта получателя с id " + request.toCardId() + " не найдена"));
    log.info("Пользователь {} инициирует перевод с карты {} на карту {}",
        username, fromCard.getNumberEncrypted(), toCard.getNumberEncrypted());

    if (!fromCard.getBankUser().getUsername().equals(username) ||
        !toCard.getBankUser().getUsername().equals(username)) {
      log.warn("Пользователь {} попытался перевести с чужой карты", username);
      throw new AccessDeniedException("Карты должны принадлежать одному пользователю");
    }

    if (fromCard.getBalance().compareTo(request.amount()) < 0) {
      log.warn("Недостаточно средств для перевода {} с карты {}", request.amount(), fromCard.getNumberEncrypted());
      var tx = new Transaction();
      tx.setFromCard(fromCard);
      tx.setToCard(toCard);
      tx.setAmount(request.amount());
      tx.setStatus(TransactionStatus.FAILED);
      transactionRepository.save(tx);
      throw new InsufficientBalanceException("Недостаточно средств для перевода");
    }

    fromCard.setBalance(fromCard.getBalance().subtract(request.amount()));
    toCard.setBalance(toCard.getBalance().add(request.amount()));

    cardRepository.save(fromCard);
    cardRepository.save(toCard);

    var tx = new Transaction();
    tx.setFromCard(fromCard);
    tx.setToCard(toCard);
    tx.setAmount(request.amount());
    tx.setStatus(TransactionStatus.SUCCESS);
    transactionRepository.save(tx);
    log.info("Успешный перевод {} с карты {} на карту {}", request.amount(), fromCard.getNumberEncrypted(), toCard.getNumberEncrypted());

    return new TransactionResponse(tx.getId(), tx.getStatus(), "Transfer completed successfully");
  }
}
