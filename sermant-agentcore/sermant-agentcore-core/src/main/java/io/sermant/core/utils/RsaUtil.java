/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.core.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Cipher;

/**
 * RsaUtil
 *
 * @author zhp
 * @since 2022-10-13
 */
public class RsaUtil {
    private static final int SIZE = 3096;

    private static final String ALGORITHM = "RSA";

    private static final String RSA_PADDING = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private static final int KEY_SIZE = 2;

    private static final String DEFAULT_ENCODE = "UTF-8";

    private RsaUtil() {
    }

    /**
     * Generate key pair
     *
     * @return key pair
     */
    public static Optional<String[]> generateKey() {
        try {
            String[] keys = new String[KEY_SIZE];
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(SIZE, new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            keys[0] = new String(Base64.getEncoder().encode(keyPair.getPrivate().getEncoded()), DEFAULT_ENCODE);
            keys[1] = new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()), DEFAULT_ENCODE);
            return Optional.of(keys);
        } catch (IOException | GeneralSecurityException e) {
            return Optional.empty();
        }
    }

    /**
     * encrypt
     *
     * @param publicKey publicKey
     * @param text text
     * @return encrypted text
     */
    public static Optional<String> encrypt(String publicKey, String text) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_PADDING);
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey.getBytes(DEFAULT_ENCODE));
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(encodedKeySpec));
            return Optional.ofNullable(new String(Base64.getEncoder().encode(cipher.doFinal(
                    text.getBytes(DEFAULT_ENCODE))), DEFAULT_ENCODE));
        } catch (IOException | GeneralSecurityException e) {
            return Optional.empty();
        }
    }

    /**
     * decrypt
     *
     * @param privateKey privateKey
     * @param text text
     * @return decrypted text
     */
    public static Optional<String> decrypt(String privateKey, String text) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_PADDING);
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKey.getBytes(DEFAULT_ENCODE));
            byte[] encryptTextByte = Base64.getDecoder().decode(text.getBytes(DEFAULT_ENCODE));
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keyFactory.generatePrivate(encodedKeySpec));
            return Optional.ofNullable(new String(cipher.doFinal(encryptTextByte), DEFAULT_ENCODE));
        } catch (IOException | GeneralSecurityException e) {
            return Optional.empty();
        }
    }
}
