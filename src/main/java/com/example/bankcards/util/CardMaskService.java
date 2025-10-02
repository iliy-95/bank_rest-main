package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardMaskService {

  public String mask(String lastFourDigits) {

    if (lastFourDigits == null || lastFourDigits.length() != 4) {

      return "****";
    }
    return "**** **** **** " + lastFourDigits;
  }
}
