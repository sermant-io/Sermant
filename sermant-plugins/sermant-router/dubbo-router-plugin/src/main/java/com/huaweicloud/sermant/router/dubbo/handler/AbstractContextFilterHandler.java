/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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
 * ContextFilter handler
 *
 * @author provenceee
 * @since 2023-02-21
 */
public abstract class AbstractContextFilterHandler implements Handler {
    /**
     * obtain the transparent transmission marker
     *
     * @param invoker invoker
     * @param invocation invocation
     * @param attachments attachments
     * @param matchKeys transparent transmission request header
     * @param injectTags stain markers
     * @return swimlane markers
     * @see com.alibaba.dubbo.rpc.Invoker
     * @see org.apache.dubbo.rpc.Invoker
     * @see com.alibaba.dubbo.rpc.Invocation
     * @see org.apache.dubbo.rpc.Invocation
     */
    public abstract Map<String, List<String>> getRequestTag(Object invoker, Object invocation,
            Map<String, Object> attachments, Set<String> matchKeys, Set<String> injectTags);

    /**
     * Obtain request tags that need to be passed through from attachments
     *
     * @param attachments attachments
     * @param keys the key of the tag to be obtained
     * @return request tags
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