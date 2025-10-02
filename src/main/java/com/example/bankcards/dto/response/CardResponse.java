package com.example.bankcards.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CardResponse(

    String message,

    UUID id,

    String numberEncrypted,

    String holderName,

    LocalDate expiryDate,

    BigDecimal balance,

    String status,

    UUID bankUser
) {

}
