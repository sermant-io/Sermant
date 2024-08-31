/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.mq.grayscale.rocketmq.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.utils.StringUtils;
import io.sermant.mq.grayscale.config.MqGrayConfigCache;
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqGrayscaleConfigUtils;

/**
 * DefaultMQPushConsumer/DefaultLitePullConsumer/DefaultMQPullConsumer Constructor method interceptor
 * gray scene reset consumerGroup with grayGroupTag
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class RocketMqConsumerConstructorInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        if (!MqGrayConfigCache.getCacheConfig().isEnabled()) {
            return context;
        }
        String grayGroupTag = RocketMqGrayscaleConfigUtils.getGrayGroupTag();
        if (StringUtils.isEmpty(grayGroupTag)) {
            return context;
        }
        String originGroup = (String) context.getArguments()[1];
        context.getArguments()[1]
                = originGroup.contains("_" + grayGroupTag) ? originGroup : originGroup + "_" + grayGroupTag;
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        return context;
    }
}
