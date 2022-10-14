/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.rabbitmq.declarer;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.rabbitmq.interceptor.RabbitmqChannelInterceptor;

import net.bytebuddy.description.method.MethodDescription;

import java.util.Arrays;
import java.util.List;

/**
 * 对rabbit mq的禁消费的一个增强定义<br>
 *
 * @author yuzl 俞真龙
 * @since 2022-10-11
 */
public class RabbitmqChannelDeclarer extends AbstractPluginDeclarer {
    /**
     * 基本的消费
     */
    public static final String BASIC_CONSUME = "basicConsume";
    /**
     * 基本消
     */
    public static final String BASIC_ACK = "basicAck";

    private static final String ENHANCE_CLASS = "com.rabbitmq.client.impl.ChannelN";
    private static final List<String> ENHANCE_METHODS = Arrays.asList(BASIC_CONSUME, BASIC_ACK);

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[] {InterceptDeclarer.build(rabbitmqMatcher(), new RabbitmqChannelInterceptor())};
    }

    private MethodMatcher rabbitmqMatcher() {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return ENHANCE_METHODS.contains(methodDescription.getActualName());
            }
        };
    }
}
