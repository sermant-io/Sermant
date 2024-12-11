/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Optional;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * sermant agent hot plugging script
 *
 * @author zhanghaopeng
 * @since 2024-12-10
 */
public class AesUtil {
    private static final int SIZE = 256;

    private static final int LENGTH = 16;

    private static final int IV_LENGTH = 12;

    private static final String ALGORITHM = "AES";

    private static final String AES_PADDING = "AES/GCM/PKCS5Padding";

    private static final String DEFAULT_ENCODE = "UTF-8";

    private AesUtil() {
    }

    public static void main(String[] var0) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("please input your password\n");
        String password = scanner.nextLine();
        scanner.close();
        Optional<String> keyOptional = generateKey();
        if (!keyOptional.isPresent()) {
            System.out.println("failed to generate key");
        } else {
            String key = (String) keyOptional.get();
            System.out.println("encryption key is " + key);
            Optional encryptedPassword = encrypt(key, password);
            if (!encryptedPassword.isPresent()) {
                System.out.println("failed to encrypted password");
            } else {
                System.out.println("encrypted password is " + (String) encryptedPassword.get());
            }
        }
    }

    /**
     * Generate key pair
     *
     * @return key pair
     */
    public static Optional<String> generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            String keys = new String(Base64.getEncoder().encode(secretKey.getEncoded()), DEFAULT_ENCODE);
            return Optional.of(keys);
        } catch (IOException | GeneralSecurityException e) {
            return Optional.empty();
        }
    }

    /**
     * encrypt
     *
     * @param key key
     * @param text text
     * @return encrypted text
     */
    public static Optional<String> encrypt(String key, String text) {
        try {
            Cipher cipher = Cipher.getInstance(AES_PADDING);
            byte[] keyBytes = Base64.getDecoder().decode(key.getBytes(DEFAULT_ENCODE));
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptBytes = cipher.doFinal(text.getBytes(DEFAULT_ENCODE));
            byte[] bytes = new byte[IV_LENGTH + text.getBytes(DEFAULT_ENCODE).length + LENGTH];
            System.arraycopy(cipher.getIV(), 0, bytes, 0, IV_LENGTH);
            System.arraycopy(encryptBytes, 0, bytes, IV_LENGTH, encryptBytes.length);
            return Optional.of(new String(Base64.getEncoder().encode(bytes), DEFAULT_ENCODE));
        } catch (IOException | GeneralSecurityException e) {
            return Optional.empty();
        }
    }
}
