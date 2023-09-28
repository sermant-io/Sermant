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

package com.huaweicloud.sermant.tag.transmission.config.strategy;

import java.util.List;

/**
 * 需要透传的key的匹配策略接口
 *
 * @author lilai
 * @since 2023-09-07
 */
public interface MatchStrategy {
    /**
     * 请求中或线程变量中的key是否匹配配置中要透传的规则
     *
     * @param key 被匹配的键
     * @param keyConfigs key的匹配配置
     * @return 匹配结果
     */
    boolean isMatch(String key, List<String> keyConfigs);
}
