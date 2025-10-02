package com.example.bankcards.dto.response;

import com.example.bankcards.entity.enums.TransactionStatus;
import java.util.UUID;

public record TransactionResponse(
    UUID transactionId,
    TransactionStatus status,
    String message
) {}
