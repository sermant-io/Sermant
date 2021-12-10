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

package com.huawei.javamesh.core.lubanops.integration.transport.http;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author
 * @date 2020/8/7 15:29
 */
public interface Singer {
    static final String SDK_SIGNING_ALGORITHM = "SDK-HMAC-SHA256";
    static final String TIME_FORMATTER = "yyyyMMdd'T'HHmmss'Z'";
    static final String SIGN_NEW_LINE = "\n";
    static final String SIGN_FIELD_HOST = "Host";

    /**
     * 对request数据进行签名，并将签名放入signature字段。
     * @param request
     *            request数据对象。
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    void sign(Request request) throws InvalidKeyException, NoSuchAlgorithmException;

}
