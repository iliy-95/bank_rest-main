package com.example.bankcards.service;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.example.bankcards.dto.request.UserRoleUpdateRequest;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.UserRoleRepository;
import com.example.bankcards.service.impl.UserServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  private static final String USERNAME = "testuser";

  private static final String ROLE_NAME = "USER";

  private BankUser user;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserRoleRepository userRoleRepository;

  @InjectMocks
  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    UserRole role = new UserRole();
    role.setName(ROLE_NAME);

    user = new BankUser();
    user.setId(USER_ID);
    user.setUsername(USERNAME);
    user.setRole(role);
    user.setEnabled(true);
  }

  @Test
  @DisplayName("Получение всех пользователей")
  void testGetAllUsers() {
    when(userRepository.findAll()).thenReturn(List.of(user));

    List<UserResponse> users = userService.getAllUsers();

    assertThat(users).hasSize(1);
    assertThat(users.get(0).username()).isEqualTo(USERNAME);
  }


  @Test
  @DisplayName("Получение пользователя по ID")
  void testGetUserById_Success() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

    UserResponse response = userService.getUserById(USER_ID);

    assertThat(response.username()).isEqualTo(USERNAME);
    assertThat(response.role()).isEqualTo(ROLE_NAME);
  }

  @Test
  @DisplayName("Получение пользователя по несуществующему ID")
  void testGetUserById_NotFound() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.getUserById(USER_ID));
  }

  @Test
  @DisplayName("Изменение роли пользователя")
  void testUpdateUserRole_Success() {
    UserRole newRole = new UserRole();
    newRole.setName("ADMIN");
    UserRoleUpdateRequest request = new UserRoleUpdateRequest("ADMIN");

    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(userRoleRepository.findByName("ADMIN")).thenReturn(Optional.of(newRole));
    when(userRepository.save(any(BankUser.class))).thenReturn(user);

    var response = userService.updateUserRole(USER_ID, request);

    assertThat(response.role()).isEqualTo("ADMIN");
    verify(userRepository, times(1)).save(user);
  }

  @Test
  @DisplayName("Изменение роли несуществующего пользователя")
  void testUpdateUserRole_UserNotFound() {
    UserRoleUpdateRequest request = new UserRoleUpdateRequest("ADMIN");
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.updateUserRole(USER_ID, request));
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Изменение роли на несуществующую")
  void testUpdateUserRole_RoleNotFound() {
    UserRoleUpdateRequest request = new UserRoleUpdateRequest("ADMIN");
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(userRoleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.updateUserRole(USER_ID, request));
  }

  @Test
  @DisplayName("Блокировка пользователя")
  void testBlockUser_Success() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    user.setEnabled(true);

    MessageResponse response = userService.blockUser(USER_ID);

    assertThat(response.message()).isEqualTo("Пользователь заблокирован");
    assertThat(user.getEnabled()).isFalse();
    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("Попытка блокировки уже заблокированного пользователя")
  void testBlockUser_AlreadyBlocked() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    user.setEnabled(false);

    MessageResponse response = userService.blockUser(USER_ID);

    assertThat(response.message()).isEqualTo("Пользователь уже заблокирован");
    verify(userRepository, never()).save(user);
  }

  @Test
  @DisplayName("Разблокировка пользователя")
  void testUnblockUser_Success() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    user.setEnabled(false);

    MessageResponse response = userService.unblockUser(USER_ID);

    assertThat(response.message()).isEqualTo("Пользователь разблокирован");
    assertThat(user.getEnabled()).isTrue();
    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("Попытка разблокировки уже активного пользователя")
  void testUnblockUser_AlreadyActive() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    user.setEnabled(true);

    MessageResponse response = userService.unblockUser(USER_ID);

    assertThat(response.message()).isEqualTo("Пользователь уже активен");
    verify(userRepository, never()).save(user);
  }
}
