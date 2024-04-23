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

package io.sermant.core.plugin.agent.transformer;

import io.sermant.core.plugin.Plugin;
import io.sermant.core.plugin.agent.adviser.AdviserScheduler;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.info.EnhancementManager;
import io.sermant.core.plugin.agent.interceptor.Interceptor;
import io.sermant.core.plugin.agent.template.BaseAdviseHandler;
import io.sermant.core.plugin.agent.template.MethodKeyCreator;

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
 * Reentrant class Transformer，After the transformation through this Transformer, when the transformation occurs,
 * this Transformer can be used again. Advice style.
 *
 * @author luanwenfei
 * @since 2023-09-09
 */
public class ReentrantTransformer extends AbstractTransformer {
    private final Plugin plugin;

    /**
     * constructor
     *
     * @param interceptDeclarers intercept declarer set
     * @param plugin belonged plugin
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
                .computeIfAbsent(adviceKey, key -> new ArrayList<>());
        Set<String> createdInterceptorForAdviceKey = plugin.getInterceptors()
                .computeIfAbsent(adviceKey, key -> new HashSet<>());
        for (Interceptor interceptor : interceptors) {
            // need to check whether the Interceptor is created
            if (checkInterceptor(adviceKey, interceptor.getClass().getCanonicalName())) {
                interceptorsForAdviceKey.add(interceptor);
                createdInterceptorForAdviceKey.add(interceptor.getClass().getCanonicalName());
            }
        }
        EnhancementManager.addEnhancements(plugin, interceptors, classLoader,
                MethodKeyCreator.getMethodDescKey(methodDesc));
        if (checkAdviceLock(adviceKey)) {
            return builder.visit(Advice.to(templateCls).on(ElementMatchers.is(methodDesc)));
        }
        return builder;
    }

    private boolean checkAdviceLock(String adviceKey) {
        if (AdviserScheduler.lock(adviceKey)) {
            // adviceKey lock is successfully obtained, then manage it in the plugin
            plugin.getAdviceLocks().add(adviceKey);
            return true;
        }
        return plugin.getAdviceLocks().contains(adviceKey);
    }

    private boolean checkInterceptor(String adviceKey, String interceptor) {
        // Whether the plugin has created an interceptor for the adviceKey
        return !plugin.getInterceptors().get(adviceKey).contains(interceptor);
    }
}