package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.BalanceResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatusEnum;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {

  CreateCardResponse createCard(CreateCardRequest request);

  CardResponse changeCardStatus(UUID cardId, CardStatusEnum status);

  MessageResponse deleteCard(UUID cardId);

  Optional<CardResponse> usersCards(UUID cardId);

  Page<CardResponse> getUserCards(String username, String search, Pageable pageable);

  Page<CardResponse> getAllCards(String search, Pageable pageable);

  MessageResponse blockCard(UUID cardId,String name);

  BalanceResponse getBalance(UUID cardId, String username);

}
