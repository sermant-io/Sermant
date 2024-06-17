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

package io.sermant.mq.grayscale.service;

import io.sermant.core.utils.StringUtils;
import io.sermant.mq.grayscale.utils.MqGrayscaleConfigUtils;

import org.apache.rocketmq.client.hook.FilterMessageContext;
import org.apache.rocketmq.client.hook.FilterMessageHook;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.Iterator;
import java.util.List;

/**
 * gray message filter service
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class MqGrayMessageFilter implements FilterMessageHook {
    private static final String CONSUME_TYPE_ALL = "all";

    private static final String CONSUME_TYPE_BASE = "base";

    @Override
    public String hookName() {
        return "MqGrayMessageFilter";
    }

    @Override
    public void filterMessage(FilterMessageContext context) {
        String grayTag = MqGrayscaleConfigUtils.getGrayEnvTag();
        String trafficGrayTag = MqGrayscaleConfigUtils.getTrafficGrayTag();
        List<MessageExt> messageExts = context.getMsgList();
        Iterator<MessageExt> iterator = messageExts.iterator();
        while (iterator.hasNext()) {
            MessageExt message = iterator.next();
            String messageGrayTag = message.getProperty(MqGrayscaleConfigUtils.MICRO_SERVICE_GRAY_TAG_KEY);
            String trafficMessageTag = message.getProperty(MqGrayscaleConfigUtils.MICRO_TRAFFIC_GRAY_TAG_KEY);
            if (StringUtils.isEmpty(grayTag)) {
                filterBasicMessage(iterator, trafficGrayTag, messageGrayTag, trafficMessageTag);
                continue;
            }
            if (!StringUtils.equals(grayTag, messageGrayTag) && StringUtils.isEmpty(trafficMessageTag)) {
                // gray environment consumer consume traffic tag or gray env tag message
                iterator.remove();
            }
        }
    }

    private void filterBasicMessage(Iterator<MessageExt> iterator, String trafficGrayTag, String messageGrayTag,
            String trafficMessageTag) {
        if (CONSUME_TYPE_ALL.equals(MqGrayscaleConfigUtils.getConsumeType())) {
            return;
        }
        if (CONSUME_TYPE_BASE.equals(MqGrayscaleConfigUtils.getConsumeType())) {
            if (!StringUtils.isEmpty(trafficMessageTag) || !StringUtils.isEmpty(messageGrayTag)) {
                iterator.remove();
            }
            return;
        }
        if (!StringUtils.isEmpty(trafficGrayTag)) {
            if (!trafficGrayTag.equals(trafficMessageTag)) {
                iterator.remove();
            }
        } else {
            if (MqGrayscaleConfigUtils.isExcludeTagsContainsTag(messageGrayTag)) {
                iterator.remove();
            }
        }
    }
}
