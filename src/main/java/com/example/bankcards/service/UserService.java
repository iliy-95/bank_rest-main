package com.example.bankcards.service;

import com.example.bankcards.dto.request.UserRoleUpdateRequest;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.dto.response.UserResponse;
import java.util.List;
import java.util.UUID;

public interface UserService {

  List<UserResponse> getAllUsers();

  UserResponse getUserById(UUID userId);

  UserResponse updateUserRole(UUID userId, UserRoleUpdateRequest request);

  MessageResponse blockUser(UUID userId);

  MessageResponse unblockUser(UUID userId);

}
