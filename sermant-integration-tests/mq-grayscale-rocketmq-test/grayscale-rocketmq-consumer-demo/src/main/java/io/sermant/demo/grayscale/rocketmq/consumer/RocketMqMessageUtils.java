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

package io.sermant.demo.grayscale.rocketmq.consumer;

import org.apache.rocketmq.common.message.MessageExt;

import java.util.HashMap;
import java.util.Map;

/**
 * message count utils
 *
 * @author chengyouling
 * @since 2024-10-30
 */
public class RocketMqMessageUtils {
    private static final String GRAY = "gray";

    private static int grayMessageCount = 0;

    private static int baseMessageCount = 0;

    private RocketMqMessageUtils() {
    }

    /**
     * convert base/gray message
     *
     * @param messageExt MessageExt
     */
    public static void convertMessageCount(MessageExt messageExt) {
        if (messageExt.getProperties() != null && GRAY.equals(messageExt.getProperties().get(
                "x_lane_canary"))) {
            grayMessageCount++;
            return;
        }
        baseMessageCount++;
    }

    /**
     * get base/gray message count
     *
     * @return message count
     */
    public static Map<String, Object> getMessageCount() {
        Map<String, Object> countMap = new HashMap<>();
        countMap.put("baseMessageCount", baseMessageCount);
        countMap.put("grayMessageCount", grayMessageCount);
        return countMap;
    }

    /**
     * clear cache count info
     */
    public static void clearMessageCount() {
        baseMessageCount = 0;
        grayMessageCount = 0;
    }
}
