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

package com.huawei.gray.feign.context;

/**
 * 下游服务名线程变量
 *
 * @author lilai
 * @since 2021-11-03
 */
public class HostContext {
    /**
     * 下游服务名
     */
    private static final ThreadLocal<String> HOST_CONTEXT = new ThreadLocal<String>();

    private HostContext() {
    }

    /**
     * 存入host
     *
     * @param hostContext host
     */
    public static void set(String hostContext) {
        HOST_CONTEXT.set(hostContext);
    }

    /**
     * 获取host
     *
     * @return host
     */
    public static String get() {
        return HOST_CONTEXT.get();
    }

    /**
     * 删除host
     */
    public static void remove() {
        HOST_CONTEXT.remove();
    }
}
