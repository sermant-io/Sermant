/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.agentcore.tests.plugin.listener;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 测试监听器类
 *
 * @author tangle
 * @since 2023-09-11
 */
public class TestListener implements DynamicConfigListener {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void process(DynamicConfigEvent event) {
        // 监听成功回执
        setListenerReceipt();
    }

    /**
     * 反射调用测试应用的监听成功标识变量
     */
    private void setListenerReceipt() {
        try {
            Class<?> targetClass = Class.forName(
                    "com.huaweicloud.agentcore.test.application.tests.dynamicconfig"
                            + ".DynamicConfigTest");
            Method targetMethod = targetClass.getMethod("setListenerSuccess", boolean.class);
            targetMethod.invoke(null, true);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException
                 | IllegalAccessException exception) {
            LOGGER.log(Level.SEVERE, "setListenerReceipt exception: {0}", exception.getMessage());
        }
    }
}
