package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserRoleUpdateRequest(
    @NotBlank(message = "Роль не может быть пустой")
    String role
) {}
