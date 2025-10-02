package com.example.bankcards.util;

import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CardNumberGenerator {

  private static final List<String> MIR_CARD_NUMBER_PREFIX = List.of("2200", "2201", "2202",
      "2204");

  private static final Random RANDOM = new Random();

  public String generateMirCard() {

    StringBuilder cardNumber = new StringBuilder(
        MIR_CARD_NUMBER_PREFIX.get(RANDOM.nextInt(MIR_CARD_NUMBER_PREFIX.size())));

    int sumForLuhn = 0;

    int totalLength = 16;
    while (cardNumber.length() < totalLength - 1) {
      int digit = RANDOM.nextInt(10);
      cardNumber.append(digit);
    }

    for (int i = 0; i < cardNumber.length(); i++) {
      int digit = cardNumber.charAt(cardNumber.length() - 1 - i) - '0';

      if (i % 2 == 0) {
        digit *= 2;
        if (digit > 9) {
          digit -= 9;
        }
      }
      sumForLuhn += digit;
    }

    int checkDigit = (10 - (sumForLuhn % 10)) % 10;

    return cardNumber.append(checkDigit).toString();
  }

}
