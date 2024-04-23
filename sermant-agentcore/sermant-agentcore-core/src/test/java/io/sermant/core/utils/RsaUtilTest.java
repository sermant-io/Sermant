package io.sermant.core.utils;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class RsaUtilTest {

    private static final String TEXT = "aaa";

    @Test
    void generateKey() {
        Optional<String[]> optional = RsaUtil.generateKey();
        Assertions.assertTrue(optional.isPresent());
        String[] key = optional.get();
        Assertions.assertEquals(2, key.length);
    }

    @Test
    void encrypt() {
        Optional<String[]> optional = RsaUtil.generateKey();
        Assertions.assertTrue(optional.isPresent());
        String[] key = optional.get();
        Optional<String> encryptTextOptional = RsaUtil.encrypt(key[1], TEXT);
        Optional<String> decryptTextOptional = RsaUtil.decrypt(key[0], encryptTextOptional.get());
        Assert.assertEquals(decryptTextOptional.get(), TEXT);
    }

    @Test
    void decrypt() {
    }
}