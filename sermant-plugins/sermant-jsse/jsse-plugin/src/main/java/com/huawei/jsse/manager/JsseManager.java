/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.jsse.manager;

import com.huawei.jsse.entity.JsseLinkInfo;
import com.huawei.jsse.entity.JsseRpcInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSSE管理类
 *
 * @author zhp
 * @since 2023-10-17
 */
public class JsseManager {
    private static final Map<String, JsseLinkInfo> JSSE_LINK_INFO_MAP = new ConcurrentHashMap<>();

    private static final Map<String, JsseRpcInfo> JSSE_RPC_INFO_MAP = new ConcurrentHashMap<>();

    private JsseManager() {
    }

    /**
     * 获取JSSE连接信息
     *
     * @return JSSE连接信息
     */
    public static Map<String, JsseLinkInfo> getJsseLinkMap() {
        return JSSE_LINK_INFO_MAP;
    }

    /**
     * 获取JSSE连接信息
     *
     * @return JSSE连接信息
     */
    public static Map<String, JsseRpcInfo> getJsseRpcMap() {
        return JSSE_RPC_INFO_MAP;
    }

    /**
     * 获取JSSE连接信息
     *
     * @param key JSSE MAP的密钥信息
     * @return Jsse信息
     */
    public static JsseLinkInfo getJsseLink(String key) {
        return JSSE_LINK_INFO_MAP.computeIfAbsent(key, s -> new JsseLinkInfo());
    }

    /**
     * 获取JSSE RPC信息
     *
     * @param key JSSE MAP的密钥信息
     * @return Jsse RPC信息
     */
    public static JsseRpcInfo getJsseRpc(String key) {
        return JSSE_RPC_INFO_MAP.computeIfAbsent(key, s -> new JsseRpcInfo());
    }
}
