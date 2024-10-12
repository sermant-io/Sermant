package io.sermant.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class AesUtilTest {
    private static final String TEXT = "aaa";

    @Test
    void generateKey() {
        Optional<String> optional = AesUtil.generateKey();
        Assertions.assertTrue(optional.isPresent());
    }

    @Test
    void encrypt() {
        Optional<String> optional = AesUtil.generateKey();
        Assertions.assertTrue(optional.isPresent());
        String key = optional.get();
        Optional<String> encryptTextOptional = AesUtil.encrypt(key, TEXT);
        Assertions.assertTrue(encryptTextOptional.isPresent());
        Optional<String> decryptTextOptional = AesUtil.decrypt(key, encryptTextOptional.get());
        Assertions.assertTrue(decryptTextOptional.isPresent());
        Assertions.assertEquals(decryptTextOptional.get(), TEXT);
    }
}
