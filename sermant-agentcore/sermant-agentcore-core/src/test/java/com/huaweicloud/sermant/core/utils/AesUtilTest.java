/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.core.utils;

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