/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.mq.prohibition.rocketmq.declarer;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.mq.prohibition.rocketmq.utils.RocketMqEnhancementHelper;

/**
 * PushConsumer interceptor declarer, supports RocketMQ 4.8+version
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public class RocketMqPushConsumerDeclarer extends AbstractPluginDeclarer {
    @Override
    public ClassMatcher getClassMatcher() {
        return RocketMqEnhancementHelper.getPushConsumerClassMatcher();
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                RocketMqEnhancementHelper.getPushConsumerStartInterceptDeclarers(),
                RocketMqEnhancementHelper.getPushConsumerSubscribeInterceptDeclarers(),
                RocketMqEnhancementHelper.getPushConsumerUnsubscribeInterceptDeclarers(),
                RocketMqEnhancementHelper.getPushConsumerShutdownInterceptDeclarers()
        };
    }
}
