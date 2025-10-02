package com.example.bankcards.controller;

import com.example.bankcards.controller.admin.UserAdminController;
import com.example.bankcards.dto.request.UserRoleUpdateRequest;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminControllerTest {

  private static final UUID TEST_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  private static final String TEST_USERNAME = "testuser";

  private static final String TEST_ROLE_NAME = "USER";

  private static final String SUCCESS_MESSAGE = "Operation completed successfully";

  private static final String BLOCK_SUCCESS_MESSAGE = "User blocked successfully";

  private static final String UNBLOCK_SUCCESS_MESSAGE = "User unblocked successfully";

  @Mock
  private UserService userService;

  @InjectMocks
  private UserAdminController userAdminController;

  private UserResponse userResponse;

  private MessageResponse blockMessageResponse;

  private MessageResponse unblockMessageResponse;

  private UserRoleUpdateRequest userRoleUpdateRequest;

  @BeforeEach
  void setUp() {
    userResponse = new UserResponse(
        TEST_USER_ID,
        TEST_USERNAME,
        TEST_ROLE_NAME,
        true
    );

    blockMessageResponse = new MessageResponse(BLOCK_SUCCESS_MESSAGE);
    unblockMessageResponse = new MessageResponse(UNBLOCK_SUCCESS_MESSAGE);

    userRoleUpdateRequest = new UserRoleUpdateRequest(TEST_ROLE_NAME);
  }

  @Test
  @DisplayName("Должен успешно получить список всех пользователей")
  void shouldSuccessfullyGetAllUsers() {
    List<UserResponse> userList = Collections.singletonList(userResponse);
    when(userService.getAllUsers()).thenReturn(userList);

    ResponseEntity<List<UserResponse>> response = userAdminController.getAllUsers();

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals(userResponse, response.getBody().get(0));
    verify(userService).getAllUsers();
  }

  @Test
  @DisplayName("Должен успешно получить пустой список пользователей")
  void shouldSuccessfullyGetEmptyUsersList() {
    List<UserResponse> emptyList = Collections.emptyList();
    when(userService.getAllUsers()).thenReturn(emptyList);

    ResponseEntity<List<UserResponse>> response = userAdminController.getAllUsers();

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().size());
    verify(userService).getAllUsers();
  }

  @Test
  @DisplayName("Должен успешно получить пользователя по ID")
  void shouldSuccessfullyGetUserById() {
    when(userService.getUserById(TEST_USER_ID)).thenReturn(userResponse);

    ResponseEntity<UserResponse> response = userAdminController.getUserById(TEST_USER_ID);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(TEST_USER_ID, response.getBody().id());
    assertEquals(TEST_USERNAME, response.getBody().username());
    assertEquals(TEST_ROLE_NAME, response.getBody().role());
    assertTrue(response.getBody().enabled());
    verify(userService).getUserById(TEST_USER_ID);
  }

  @Test
  @DisplayName("Должен успешно обновить роль пользователя")
  void shouldSuccessfullyUpdateUserRole() {
    when(userService.updateUserRole(eq(TEST_USER_ID), any(UserRoleUpdateRequest.class)))
        .thenReturn(userResponse);

    ResponseEntity<UserResponse> response = userAdminController.updateUserRole(TEST_USER_ID,
        userRoleUpdateRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(TEST_USER_ID, response.getBody().id());
    assertEquals(TEST_ROLE_NAME, response.getBody().role());
    verify(userService).updateUserRole(TEST_USER_ID, userRoleUpdateRequest);
  }

  @Test
  @DisplayName("Должен успешно заблокировать пользователя")
  void shouldSuccessfullyBlockUser() {
    when(userService.blockUser(TEST_USER_ID)).thenReturn(blockMessageResponse);

    ResponseEntity<MessageResponse> response = userAdminController.blockUser(TEST_USER_ID);

    assertNotNull(response);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(BLOCK_SUCCESS_MESSAGE, response.getBody().message());
    verify(userService).blockUser(TEST_USER_ID);
  }

  @Test
  @DisplayName("Должен успешно разблокировать пользователя")
  void shouldSuccessfullyUnblockUser() {
    when(userService.unblockUser(TEST_USER_ID)).thenReturn(unblockMessageResponse);

    ResponseEntity<MessageResponse> response = userAdminController.unblockUser(TEST_USER_ID);

    assertNotNull(response);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(UNBLOCK_SUCCESS_MESSAGE, response.getBody().message());
    verify(userService).unblockUser(TEST_USER_ID);
  }

  @Test
  @DisplayName("Должен корректно создавать UserResponse со всеми полями")
  void shouldCorrectlyCreateUserResponseWithAllFields() {
    UserResponse testUserResponse = new UserResponse(
        TEST_USER_ID,
        TEST_USERNAME,
        TEST_ROLE_NAME,
        true
    );

    assertEquals(TEST_USER_ID, testUserResponse.id());
    assertEquals(TEST_USERNAME, testUserResponse.username());
    assertEquals(TEST_ROLE_NAME, testUserResponse.role());
    assertTrue(testUserResponse.enabled());
  }

  @Test
  @DisplayName("Должен корректно создавать UserRoleUpdateRequest со всеми полями")
  void shouldCorrectlyCreateUserRoleUpdateRequestWithAllFields() {
    UserRoleUpdateRequest testRequest = new UserRoleUpdateRequest(TEST_ROLE_NAME);

    assertEquals(TEST_ROLE_NAME, testRequest.role());
  }

  @Test
  @DisplayName("Должен корректно создавать MessageResponse со всеми полями")
  void shouldCorrectlyCreateMessageResponseWithAllFields() {
    MessageResponse testMessageResponse = new MessageResponse(SUCCESS_MESSAGE);

    assertEquals(SUCCESS_MESSAGE, testMessageResponse.message());
  }
}
