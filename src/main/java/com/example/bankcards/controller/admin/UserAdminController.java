package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.request.UserRoleUpdateRequest;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tags(value = {
    @Tag(name = "UserAdmin", description = "Операции с пользователями для администратора"),
    @Tag(name = "Admin")
})
public class UserAdminController {

  private final UserService userService;

  @Operation(
      summary = "Получить список всех пользователей",
      description = "Администратор с ролью ADMIN может получить список всех пользователей."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен",
          content = @Content(mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
      @ApiResponse(responseCode = "403", description = "Доступ запрещён")
  })
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Tag(name = "Admin")
  public ResponseEntity<List<UserResponse>> getAllUsers() {

    return ResponseEntity.ok(userService.getAllUsers());
  }

  @Operation(
      summary = "Получить пользователя по ID",
      description = "Администратор может получить информацию о пользователе по его ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Пользователь успешно найден",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserResponse.class))),
      @ApiResponse(responseCode = "404", description = "Пользователь с таким ID не найден"),
      @ApiResponse(responseCode = "403", description = "Доступ запрещён")
  })
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Tag(name = "Admin")
  public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {

    return ResponseEntity.ok(userService.getUserById(id));
  }

  @Operation(
      summary = "Обновить роль пользователя",
      description = "Администратор может изменить роль пользователя."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Роль пользователя успешно обновлена",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserResponse.class))),
      @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
      @ApiResponse(responseCode = "404", description = "Пользователь с таким ID не найден"),
      @ApiResponse(responseCode = "403", description = "Доступ запрещён")
  })
  @PutMapping("/{id}/role")
  @PreAuthorize("hasRole('ADMIN')")
  @Tag(name = "Admin")
  public ResponseEntity<UserResponse> updateUserRole(@PathVariable UUID id,
      @Valid @RequestBody UserRoleUpdateRequest request) {

    return ResponseEntity.ok(userService.updateUserRole(id, request));
  }

  @Operation(
      summary = "Заблокировать пользователя",
      description = "Администратор блокирует пользователя."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Пользователь успешно заблокирован"),
      @ApiResponse(responseCode = "404", description = "Пользователь с таким ID не найден"),
      @ApiResponse(responseCode = "403", description = "Доступ запрещён")
  })
  @PatchMapping("/{id}/block")
  @PreAuthorize("hasRole('ADMIN')")
  @Tag(name = "Admin")
  public ResponseEntity<MessageResponse> blockUser(@PathVariable UUID id) {

    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(userService.blockUser(id));
  }

  @Operation(
      summary = "Разблокировать пользователя",
      description = "Администратор разблокирует пользователя."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Пользователь успешно разблокирован"),
      @ApiResponse(responseCode = "404", description = "Пользователь с таким ID не найден"),
      @ApiResponse(responseCode = "403", description = "Доступ запрещён")
  })
  @PatchMapping("/{id}/unblock")
  @PreAuthorize("hasRole('ADMIN')")
  @Tag(name = "Admin")
  public ResponseEntity<MessageResponse> unblockUser(@PathVariable UUID id) {

    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(userService.unblockUser(id));
  }

}

