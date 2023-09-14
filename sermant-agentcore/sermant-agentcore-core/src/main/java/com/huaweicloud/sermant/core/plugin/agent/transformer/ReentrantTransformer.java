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

package com.huaweicloud.sermant.core.plugin.agent.transformer;

import com.huaweicloud.sermant.core.plugin.Plugin;
import com.huaweicloud.sermant.core.plugin.agent.adviser.AdviserScheduler;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.agent.template.BaseAdviseHandler;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 可重入的类的Transformer，在通过该Transformer转换过后，当发生重转换时还可以再次通过该Transformer进行转换，advice风格
 *
 * @author luanwenfei
 * @since 2023-09-09
 */
public class ReentrantTransformer extends AbstractTransformer {
    private final Plugin plugin;

    /**
     * 构造方法
     *
     * @param interceptDeclarers 拦截声明器数组
     * @param plugin 归属的插件
     */
    public ReentrantTransformer(InterceptDeclarer[] interceptDeclarers, Plugin plugin) {
        super(interceptDeclarers);
        this.plugin = plugin;
    }

    @Override
    protected Builder<?> resolve(Builder<?> builder, InDefinedShape methodDesc, List<Interceptor> interceptors,
            Class<?> templateCls, ClassLoader classLoader)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        final String adviceKey = getAdviceKey(templateCls, classLoader, methodDesc);
        List<Interceptor> interceptorsForAdviceKey = BaseAdviseHandler.getInterceptorListMap()
                .computeIfAbsent(adviceKey, k -> new ArrayList<>());
        Set<String> createdInterceptorForAdviceKey = plugin.getInterceptors()
                .computeIfAbsent(adviceKey, k -> new HashSet<>());
        for (Interceptor interceptor : interceptors) {
            // 需要先校验该Interceptor是否被创建过
            if (checkInterceptor(adviceKey, interceptor.getClass().getCanonicalName())) {
                interceptorsForAdviceKey.add(interceptor);
                createdInterceptorForAdviceKey.add(interceptor.getClass().getCanonicalName());
            }
        }
        if (checkAdviceLock(adviceKey)) {
            return builder.visit(Advice.to(templateCls).on(ElementMatchers.is(methodDesc)));
        }
        return builder;
    }

    private boolean checkAdviceLock(String adviceKey) {
        if (AdviserScheduler.lock(adviceKey)) {
            // 获取adviceKey锁成功，将其在归属插件中管理
            plugin.getAdviceLocks().add(adviceKey);
            return true;
        }
        return plugin.getAdviceLocks().contains(adviceKey);
    }

    private boolean checkInterceptor(String adviceKey, String interceptor) {
        // 插件是否对该adviceKey创建过拦截器
        return !plugin.getInterceptors().get(adviceKey).contains(interceptor);
    }
}