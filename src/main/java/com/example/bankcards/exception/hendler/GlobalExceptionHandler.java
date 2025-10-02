package com.example.bankcards.exception.hendler;

import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.CardExpiredException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.exception.InvalidRoleException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.OperationNotAllowedException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(LocalDateTime.now(), 500, ex.getMessage(), ""));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(LocalDateTime.now(), 404, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(LocalDateTime.now(), 403, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(LocalDateTime.now(), 400, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler({InsufficientBalanceException.class,
        CardBlockedException.class,
        CardExpiredException.class,
        InvalidRoleException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(LocalDateTime.now(), 400, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(UserAlreadyExistsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(LocalDateTime.now(), 409, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(OperationNotAllowedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(LocalDateTime.now(), 403, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(LocalDateTime.now(), 401, "Неверный логин или пароль", request.getRequestURI()));
    }

}

