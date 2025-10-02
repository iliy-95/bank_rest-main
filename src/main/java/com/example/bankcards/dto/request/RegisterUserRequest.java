package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    String name,

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 100 символов")
    String password,

    @NotBlank(message = "Полное имя не может быть пустым")
    @Size(min = 3, max = 100, message = "Полное имя должно быть от 3 до 100 символов")
    String fullName
) {}
