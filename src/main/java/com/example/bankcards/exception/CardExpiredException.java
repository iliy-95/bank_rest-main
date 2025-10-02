package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CardExpiredException extends RuntimeException {

  public CardExpiredException(String message) {
    super(message);
  }
}
