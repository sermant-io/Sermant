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

package io.sermant.mq.grayscale.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.mq.grayscale.service.MqConsumerGroupAutoCheck;
import io.sermant.mq.grayscale.utils.MqGrayscaleConfigUtils;

import java.util.Optional;

/**
 * consumer group builder interceptor
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class MqPullConsumerConstructorInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (MqGrayscaleConfigUtils.isPlugEnabled()) {
            String grayEnvTag = MqGrayscaleConfigUtils.getGrayEnvTag();
            Optional<Object> originGroupOptional = ReflectUtils.getFieldValue(context.getObject(), "consumerGroup");
            if (StringUtils.isEmpty(grayEnvTag)) {
                originGroupOptional.ifPresent(o -> MqConsumerGroupAutoCheck.setOriginGroup((String) o));
                return context;
            }
            String originGroup = "";
            if (originGroupOptional.isPresent()) {
                originGroup = (String) originGroupOptional.get();
            }

            // consumerGroup format is ^[%|a-zA-Z0-9_-]+$
            String newConsumerGroup = originGroup;
            if (!originGroup.contains(grayEnvTag)) {
                newConsumerGroup = originGroup + "_" + grayEnvTag;
            }
            ReflectUtils.setFieldValue(context.getObject(), "consumerGroup", newConsumerGroup);
        }
        return context;
    }
}
