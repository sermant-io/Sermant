/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.lubanops.integration.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.huawei.javamesh.core.lubanops.integration.Constants;
import com.huawei.javamesh.core.lubanops.integration.access.HMacAlgorithm;
import com.huawei.javamesh.core.lubanops.integration.access.HMacSignatureUtil;

/**
 * @author
 * @since 2020/5/16
 **/
public class UriUtil {
    private static String encodeString(String s) {
        try {
            return URLEncoder.encode(s, Constants.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    /**
     * 构造socket连接的uri
     *
     * @param address    websocket的地址
     * @param ak         access key
     * @param sk         secret key
     * @param instanceId 实例ID
     * @return
     */
    public static String buildUri(String address, String ak, String sk, long instanceId) {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String signature = HMacSignatureUtil.getHmacSign(ts, sk, HMacAlgorithm.HmacSHA256);
        StringBuilder sb = new StringBuilder();
        sb.append(address);
        sb.append("/access-server/");
        sb.append(encodeString(ak));
        sb.append("/").append(ts).append("/");
        sb.append(signature).append("/").append(instanceId);
        return sb.toString();
    }
}
