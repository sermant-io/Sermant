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

package com.huaweicloud.sermant.router.spring.handler;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * web拦截器处理器
 *
 * @author provenceee
 * @since 2023-02-21
 */
public abstract class AbstractRequestTagHandler extends AbstractHandler {
    /**
     * 获取透传的标记
     *
     * @param path 请求路径
     * @param methodName http方法
     * @param headers http请求头
     * @param parameters url参数
     * @param keys 透传标记key
     * @return 透传的标记
     */
    public abstract Map<String, List<String>> getRequestTag(String path, String methodName,
            Map<String, List<String>> headers, Map<String, String[]> parameters, Keys keys);

    /**
     * 透传标记key实体
     *
     * @author provenceee
     * @since 2023-02-21
     */
    public static class Keys {
        private final Set<String> matchKeys;

        private final Set<String> injectTags;

        /**
         * 构造方法
         *
         * @param matchKeys 标签路由透传标记
         * @param injectTags 泳道透传标记
         */
        public Keys(Set<String> matchKeys, Set<String> injectTags) {
            this.matchKeys = matchKeys;
            this.injectTags = injectTags;
        }

        public Set<String> getMatchKeys() {
            return matchKeys;
        }

        public Set<String> getInjectTags() {
            return injectTags;
        }
    }
}