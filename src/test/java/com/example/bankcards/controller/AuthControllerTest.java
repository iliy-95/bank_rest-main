package com.example.bankcards.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegisterUserRequest;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.dto.response.RegistrationResponse;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private static final String USERNAME = "testuser";

  private static final String PASSWORD = "password123";

  private static final String FULL_NAME = "Тестовый Пользователь";

  private static final String TOKEN = "jwt-token";

  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @Mock
  private AuthService authService;

  @InjectMocks
  private AuthController authController;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
  }

  @Test
  @DisplayName("Успешная регистрация пользователя")
  void testRegisterUser_Success() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest(USERNAME, PASSWORD, FULL_NAME);
    RegistrationResponse response = new RegistrationResponse("Вы успешно зарегистрировались.",
        TOKEN);

    when(authService.registerUser(any(RegisterUserRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value(TOKEN));

    verify(authService, times(1)).registerUser(any(RegisterUserRequest.class));
  }

  @Test
  @DisplayName("Попытка регистрации существующего пользователя")
  void testRegisterUser_AlreadyExists() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest(USERNAME, PASSWORD, FULL_NAME);

    when(authService.registerUser(any(RegisterUserRequest.class)))
        .thenThrow(new UserAlreadyExistsException("Пользователь с таким именем уже существует"));

    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());

    verify(authService, times(1)).registerUser(any(RegisterUserRequest.class));
  }

  @Test
  @DisplayName("Успешный вход пользователя")
  void testLoginUser_Success() throws Exception {
    LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
    var response = new MessageResponse(TOKEN);

    when(authService.loginUser(any(LoginRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(TOKEN));

    verify(authService, times(1)).loginUser(any(LoginRequest.class));
  }

}
