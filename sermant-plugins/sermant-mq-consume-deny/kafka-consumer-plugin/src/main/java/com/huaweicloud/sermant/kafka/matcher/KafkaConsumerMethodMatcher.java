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

package com.huaweicloud.sermant.kafka.matcher;

import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

import net.bytebuddy.description.method.MethodDescription;

import java.util.Arrays;
import java.util.List;

/**
 * 一个自定义的方法匹配器<br>
 *
 * @author yuzl 俞真龙
 * @since 2022-10-09
 */
public abstract class KafkaConsumerMethodMatcher extends MethodMatcher {
    private static final List<String> INCLUDE_METHOD = Arrays.asList("assignment", "subscription", "subscribe",
        "assign", "unsubscribe", "poll", "commitSync", "seek", "seekToBeginning", "seekToEnd", "position", "committed",
        "metrics", "partitionsFor", "listTopics", "paused", "pause", "resume", "offsetsForTimes", "beginningOffsets",
        "endOffsets", "groupMetadata", "enforceRebalance", "wakeup");

    /**
     * 非close方法，都需要增强
     *
     * @return {@link MethodMatcher}
     */
    public static MethodMatcher matchKafkaMethod() {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return methodDescription.isPublic() && !methodDescription.isStatic()
                    && INCLUDE_METHOD.contains(methodDescription.getActualName());
            }
        };
    }
}
