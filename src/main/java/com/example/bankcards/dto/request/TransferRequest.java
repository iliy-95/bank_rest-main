package com.example.bankcards.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
    @NotNull(message = "ID карты отправителя не может быть пустым")
    UUID fromCardId,

    @NotNull(message = "ID карты получателя не может быть пустым")
    UUID toCardId,

    @NotNull(message = "Сумма перевода не может быть пустой")
    @DecimalMin(value = "0.01", message = "Сумма перевода должна быть больше 0")
    BigDecimal amount
) {}
