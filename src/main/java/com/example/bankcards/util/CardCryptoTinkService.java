package com.example.bankcards.util;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class CardCryptoTinkService {

  private final Aead aead;

  public CardCryptoTinkService() {
    try {
      AeadConfig.register();

      KeysetHandle handle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM);

      this.aead = handle.getPrimitive(Aead.class);

    } catch (GeneralSecurityException e) {

      throw new RuntimeException("Ошибка инициализации Tink", e);
    }
  }

  public String encrypt(String pan) {
    try {

      byte[] ct = aead.encrypt(pan.getBytes(StandardCharsets.UTF_8), null);

      return Base64.getEncoder().encodeToString(ct);

    } catch (GeneralSecurityException e) {

      throw new RuntimeException("Ошибка шифрования PAN", e);
    }
  }

}

