/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.huaweicloud.sermant.kafka.declarer;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.kafka.interceptor.KafkaConsumerConstructorInterceptor;
import com.huaweicloud.sermant.kafka.interceptor.KafkaConsumerMethodInterceptor;
import com.huaweicloud.sermant.kafka.matcher.KafkaConsumerMethodMatcher;

/**
 * kakfa消费端消费declarer<br>
 *
 * @author yuzl 俞真龙
 * @since 2022-10-09
 */
public class KafkaConsumerDeclarer extends AbstractPluginDeclarer {
    private static final String ENHANCE_CLASS = "org.apache.kafka.clients.consumer.KafkaConsumer";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[] {
            InterceptDeclarer.build(KafkaConsumerMethodMatcher.matchKafkaMethod(),
                new KafkaConsumerMethodInterceptor()),
            InterceptDeclarer.build(MethodMatcher.isConstructor(), new KafkaConsumerConstructorInterceptor())};
    }
}
