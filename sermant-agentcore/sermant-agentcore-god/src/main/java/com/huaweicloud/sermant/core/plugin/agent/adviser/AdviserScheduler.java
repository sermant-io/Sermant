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

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adviser scheduler
 *
 * @author luanwenfei
 * @since 2023-04-11
 */
public class AdviserScheduler {
    private static final ArrayList<AdviserInterface> ADVISERS = new ArrayList<>();

    private static final Map<String, Boolean> ADVICE_LOCKS = new ConcurrentHashMap<>();

    private AdviserScheduler() {
    }

    /**
     * register Adviser
     *
     * @param adviser adviser
     */
    public static void registry(AdviserInterface adviser) {
        ADVISERS.add(adviser);
    }

    /**
     * Unregister Adviser
     *
     * @param adviser adviser
     */
    public static void unRegistry(AdviserInterface adviser) {
        ADVISERS.remove(adviser);
    }

    /**
     * The Adviser logic of the method entry
     *
     * @param context execute context
     * @param adviceKey The advice keyword consists of the class and method description, the advice template, and the
     * classloader for the enhanced class
     *
     * @return ExecuteContext
     * @throws Throwable Throwable
     */
    public static ExecuteContext onMethodEnter(Object context, String adviceKey) throws Throwable {
        ExecuteContext executeContext = (ExecuteContext) context;

        // In multi-sermant scenario, method enter is executed in sequence
        for (AdviserInterface currentAdviser : ADVISERS) {
            if (currentAdviser != null) {
                executeContext = currentAdviser.onMethodEnter(executeContext, adviceKey);
            }
        }
        return executeContext;
    }

    /**
     * The Adviser logic of the method exit
     *
     * @param context execute context
     * @param adviceKey The advice keyword consists of the class and method description, the advice template, and the
     * classloader for the enhanced class
     *
     * @return ExecuteContext
     * @throws Throwable Throwable
     */
    public static ExecuteContext onMethodExit(Object context, String adviceKey) throws Throwable {
        ExecuteContext executeContext = (ExecuteContext) context;

        // In multi-sermant scenario, method exit is executed in reverse order
        for (int i = ADVISERS.size() - 1; i >= 0; i--) {
            AdviserInterface currentAdviser = ADVISERS.get(i);
            if (currentAdviser != null) {
                executeContext = currentAdviser.onMethodExit(executeContext, adviceKey);
            }
        }
        return executeContext;
    }

    /**
     * Add an advice lock to the adviceKey
     *
     * @param adviceKey Indicate the enhancement location
     * @return Whether the lock can be acquired
     */
    public static boolean lock(String adviceKey) {
        Boolean adviceLock = ADVICE_LOCKS.get(adviceKey);
        if (adviceLock == null || !adviceLock) {
            ADVICE_LOCKS.put(adviceKey, Boolean.TRUE);
            return true;
        }
        return false;
    }

    /**
     * Release the advice lock on the adviceKey
     *
     * @param adviceKey Indicate the enhancement location
     */
    public static void unLock(String adviceKey) {
        ADVICE_LOCKS.put(adviceKey, Boolean.FALSE);
    }
}
