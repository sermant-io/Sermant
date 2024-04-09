/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.integration.service;

import com.huaweicloud.integration.service.impl.FlowControlServiceImpl;

import org.apache.dubbo.rpc.RpcException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 重试
 *
 * @author zhouss
 * @since 2022-09-19
 */
public class FlowRetryExServiceImpl extends FlowControlServiceImpl {
    private static final int MAX_RETRY_TIMES = 3;

    private final Map<String, Integer> counterMap = new ConcurrentHashMap<>();

    @Override
    public String retry(String invocationId) {
        counterMap.putIfAbsent(invocationId, 0);
        counterMap.put(invocationId, counterMap.get(invocationId) + 1);

        int retry = counterMap.get(invocationId);

        if (retry >= MAX_RETRY_TIMES) {
            return String.valueOf(retry);
        }
        throw new RpcException("retry aggin");
    }
}
