package com.example.bankcards.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(
    UUID cardId,
    BigDecimal balance,
    String holderName
) {}
