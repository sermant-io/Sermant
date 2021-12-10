/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.lubanops.integration.transport.http;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.huawei.sermant.core.lubanops.integration.enums.SignatureAlgorithm;

/**
 * @author
 * @date 2020/8/7 17:34
 */
public abstract class AbstractSigner implements Singer {

    public final byte[] deriveSigningKey(String secret) {
        return secret.getBytes(Charset.forName("UTF-8"));
    }

    public byte[] sign(byte[] data, byte[] key, SignatureAlgorithm algorithm)
            throws InvalidKeyException, NoSuchAlgorithmException {
        Mac mac = Mac.getInstance(algorithm.toString());
        mac.init(new SecretKeySpec(key, algorithm.toString()));
        return mac.doFinal(data);
    }

    public final byte[] computeSignature(String str, byte[] signingKey)
            throws InvalidKeyException, NoSuchAlgorithmException {
        return this.sign(str.getBytes(Charset.forName("UTF-8")), signingKey, SignatureAlgorithm.HmacSHA256);
    }

    public byte[] hash(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(str.getBytes(Charset.forName("UTF-8")));
            return md.digest();
        } catch (NoSuchAlgorithmException var3) {
            return null;
        }
    }

}
