package com.example.bankcards.controller;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegisterUserRequest;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.dto.response.RegistrationResponse;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Регистрация и авторизация пользователей")
public class AuthController {

  private final AuthService authService;

  @Operation(
      summary = "Регистрация нового пользователя",
      description = "Создает нового пользователя с ролью USER и возвращает JWT токен"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = RegistrationResponse.class))),
      @ApiResponse(responseCode = "400", description = "Некорректные данные запроса",
          content = @Content),
      @ApiResponse(responseCode = "409", description = "Пользователь с таким username уже существует",
          content = @Content)
  })
  @PostMapping("/register")
  public ResponseEntity<RegistrationResponse> registerUser(
      @RequestBody @Valid RegisterUserRequest request) {

    return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(request));
  }

  @Operation(
      summary = "Вход пользователя (login)",
      description = "Аутентифицирует пользователя и возвращает JWT токен"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Успешный вход пользователя",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = MessageResponse.class))),
      @ApiResponse(responseCode = "401", description = "Неверный username или пароль", content = @Content),
      @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content)
  })
  @PostMapping("/login")
  public ResponseEntity<MessageResponse> loginUser(
      @RequestBody @Valid LoginRequest request) {

    return ResponseEntity.ok(authService.loginUser(request));
  }

}

