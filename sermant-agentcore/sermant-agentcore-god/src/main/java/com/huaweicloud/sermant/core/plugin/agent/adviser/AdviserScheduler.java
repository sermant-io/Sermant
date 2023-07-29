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

package com.huaweicloud.sermant.core.plugin.agent.adviser;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * 转换器调度器
 *
 * @author luanwenfei
 * @since 2023-04-11
 */
public class AdviserScheduler {
    private static AdviserInterface currentAdviser;

    private AdviserScheduler() {
    }

    /**
     * 注册一个 Adviser
     *
     * @param adviser adviser
     */
    public static void registry(AdviserInterface adviser) {
        currentAdviser = adviser;
    }

    /**
     * 取消注册一个 Adviser
     *
     * @param adviser adviser
     */
    public static void unRegistry(AdviserInterface adviser) {
        currentAdviser = null;
    }

    /**
     * 调度方法入口的Adviser逻辑
     *
     * @param context 执行上下文
     * @param adviceKey advice的关键字，由类和方法描述、advice模板、被增强类的类加载器组成
     * @return 执行上下文
     * @throws Throwable Throwable
     */
    public static ExecuteContext onMethodEnter(Object context, String adviceKey) throws Throwable {
        ExecuteContext executeContext = (ExecuteContext) context;
        if (currentAdviser != null) {
            executeContext = currentAdviser.onMethodEnter(executeContext, adviceKey);
        }
        return executeContext;
    }

    /**
     * 调度方法出口的Adviser逻辑
     *
     * @param context 执行上下文
     * @param adviceKey advice的关键字，由类和方法描述、advice模板、被增强类的类加载器组成
     * @return 执行上下文
     * @throws Throwable Throwable
     */
    public static ExecuteContext onMethodExit(Object context, String adviceKey) throws Throwable {
        ExecuteContext executeContext = (ExecuteContext) context;
        if (currentAdviser != null) {
            executeContext = currentAdviser.onMethodExit(executeContext, adviceKey);
        }
        return executeContext;
    }
}
