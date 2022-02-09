/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.plugins.luban.adaptor.service;

import com.lubanops.apm.core.utils.AgentPath;

import net.bytebuddy.asm.Advice;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * 用于非侵入式地修改lubanops代码的adviser，将其增强逻辑屏蔽，同时取出监听器列表
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class LubanTransformerAdviser {
    private LubanTransformerAdviser() {
    }

    /**
     * 方法调用前置触发点：
     * <pre>
     *     1.设置{@link Instrumentation}，还原原本业务，该值用于重定义类
     *     2.构建{@link ClassLoader}，加载所有插件包，还原原本业务
     *     3.调用{@link com.huawei.sermant.plugins.luban.adaptor.collector.LubanListenerCollector#initialize}初始化
     * </pre>
     *
     * @param cls             被增强类，即{@code com.lubanops.apm.core.transformer.TransformerManager}
     * @param instrumentation Instrumentation对象
     * @return 跳过主要逻辑，恒为{@code true}
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onMethodEnter(@Advice.Origin Class<?> cls,
            @Advice.Argument(0) Instrumentation instrumentation) {
        try {
            cls.getDeclaredMethod("setInstrumentation", Instrumentation.class).invoke(null, instrumentation);
            final ClassLoader classLoader = new URLClassLoader((
                    (List<URL>) cls.getDeclaredMethod("getLibUrl", String.class)
                            .invoke(null, AgentPath.getInstance().getPluginsPath())
            ).toArray(new URL[0]));
            final Class<?> collectorCls = ClassLoader.getSystemClassLoader()
                    .loadClass("com.huawei.sermant.plugins.luban.adaptor.collector.LubanListenerCollector");
            collectorCls.getDeclaredMethod("initialize", ClassLoader.class).invoke(null, classLoader);
        } catch (Throwable ignored) {
            // Cannot find any medium to write out the exception log.
        }
        return true;
    }
}
