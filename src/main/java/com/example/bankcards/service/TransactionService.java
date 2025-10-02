package com.example.bankcards.service;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.TransactionResponse;

public interface TransactionService {

  TransactionResponse transfer(TransferRequest request, String username);

}
