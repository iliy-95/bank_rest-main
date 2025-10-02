package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.UserRoleUpdateRequest;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.UserRoleRepository;
import com.example.bankcards.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  private final UserRoleRepository userRoleRepository;

  @Override
  public List<UserResponse> getAllUsers() {

    return userRepository.findAll()
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  public UserResponse getUserById(UUID userId) {

    var user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    log.info("Получены данные пользователя: {}", user.getUsername());

    return mapToResponse(user);
  }

  @Override
  @Transactional
  public UserResponse updateUserRole(UUID userId, UserRoleUpdateRequest request) {

    var user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

    var role = userRoleRepository.findByName(request.role())
        .orElseThrow(() -> new NotFoundException("Роль " + request.role() + " не найдена"));

    user.setRole(role);
    userRepository.save(user);

    log.info("Роль пользователя {} изменена на {}", user.getUsername(), role.getName());

    return mapToResponse(user);
  }

  @Override
  @Transactional
  public MessageResponse blockUser(UUID userId) {

    var user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

    if (Boolean.FALSE.equals(user.getEnabled())) {
      log.warn("Попытка заблокировать уже заблокированного пользователя {}", user.getUsername());
      return new MessageResponse("Пользователь уже заблокирован");
    }

    user.setEnabled(false);
    userRepository.save(user);
    log.info("Пользователь {} заблокирован", user.getUsername());

    return new MessageResponse("Пользователь заблокирован");
  }

  @Override
  @Transactional
  public MessageResponse unblockUser(UUID userId) {

    var user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

    if (user.getEnabled()) {
      log.warn("Попытка разблокировать уже активного пользователя {}", user.getUsername());
      return new MessageResponse("Пользователь уже активен");
    }

    user.setEnabled(true);
    userRepository.save(user);
    log.info("Пользователь {} разблокирован", user.getUsername());

    return new MessageResponse("Пользователь разблокирован");

  }

  private UserResponse mapToResponse(BankUser user) {

    return new UserResponse(user.getId(), user.getUsername(), user.getRole().getName(),
        user.getEnabled());
  }
}
