package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegisterUserRequest;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.dto.response.RegistrationResponse;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.UserRoleRepository;
import com.example.bankcards.security.JwtToken;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;

  private final UserRoleRepository userRoleRepository;

  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  private final UserDetailsServiceImpl userDetailsService;

  private final JwtToken jwtToken;

  private final AuthenticationManager authenticationManager;

  @Override
  @Transactional
  public RegistrationResponse registerUser(RegisterUserRequest request) {

    if (userRepository.existsByUsername(request.name())) {
      log.warn("Попытка регистрации существующего пользователя: {}", request.name());
      throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
    }

    UserRole role = userRoleRepository.findByName("USER")
        .orElseThrow(() -> {
          log.error("Роль USER не найдена при регистрации пользователя {}", request.name());
          return new NotFoundException("Роль USER не найдена");
        });

    BankUser newUser = BankUser.builder()
        .username(request.name())
        .password(bCryptPasswordEncoder.encode(request.password()))
        .fullName(request.fullName())
        .role(role)
        .enabled(true)
        .build();

    userRepository.save(newUser);
    log.info("Зарегистрирован новый пользователь: {}", newUser.getUsername());

    UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getUsername());

    return new RegistrationResponse("Вы успешно зарегистрировались.",
        jwtToken.generatorToken(userDetails));
  }

  @Override
  public MessageResponse loginUser(LoginRequest request) {

    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.name(), request.password())
      );

      SecurityContextHolder.getContext().setAuthentication(authentication);
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();

      String token = jwtToken.generatorToken(userDetails);

      log.info("Пользователь {} успешно вошёл в систему", request.name());

      return new MessageResponse(token);

    } catch (BadCredentialsException ex) {
      log.warn("Неудачная попытка входа для пользователя {}", request.name());

      throw ex;
    }
  }
}
