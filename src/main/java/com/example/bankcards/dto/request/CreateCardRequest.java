package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCardRequest(
    @NotNull(message = "ID пользователя не может быть пустым")
    UUID userId
) {}