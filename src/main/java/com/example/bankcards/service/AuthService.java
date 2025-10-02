package com.example.bankcards.service;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegisterUserRequest;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.dto.response.RegistrationResponse;

public interface AuthService {

  RegistrationResponse registerUser(RegisterUserRequest request);

  MessageResponse loginUser(LoginRequest request);

}
