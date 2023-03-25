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

package com.huaweicloud.sermant.router.dubbo.handler;

import com.huaweicloud.sermant.router.common.handler.Handler;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * ContextFilter处理器
 *
 * @author provenceee
 * @since 2023-02-21
 */
public abstract class AbstractContextFilterHandler implements Handler {
    /**
     * 获取透传标记
     *
     * @param invoker invoker
     * @param invocation invocation
     * @param attachments attachments
     * @param matchKeys 透传请求头
     * @param injectTags 染色标记
     * @return 泳道标记
     * @see com.alibaba.dubbo.rpc.Invoker
     * @see org.apache.dubbo.rpc.Invoker
     * @see com.alibaba.dubbo.rpc.Invocation
     * @see org.apache.dubbo.rpc.Invocation
     */
    public abstract Map<String, List<String>> getRequestTag(Object invoker, Object invocation,
            Map<String, Object> attachments, Set<String> matchKeys, Set<String> injectTags);

    /**
     * 从attachments中，获取需要透传的请求标记
     *
     * @param attachments attachments
     * @param keys 需要获取的标记的key
     * @return 请求标记
     */
    protected Map<String, List<String>> getRequestTag(Map<String, Object> attachments, Set<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> tag = new HashMap<>();
        keys.forEach(key -> {
            if (attachments.containsKey(key)) {
                String value = Optional.ofNullable(attachments.get(key)).map(String::valueOf).orElse(null);
                tag.put(key, Collections.singletonList(value));
            }
        });
        return tag;
    }
}