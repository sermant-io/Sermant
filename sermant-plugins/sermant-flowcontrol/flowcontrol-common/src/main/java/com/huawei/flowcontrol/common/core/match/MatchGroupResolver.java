/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.core.match;

import com.huawei.flowcontrol.common.core.resolver.AbstractResolver;

/**
 * 业务组
 *
 * @author zhouss
 * @since 2021-11-16
 */
public class MatchGroupResolver extends AbstractResolver<BusinessMatcher> {
    /**
     * 业务场景匹配 键值
     */
    public static final String CONFIG_KEY = "servicecomb.matchGroup";

    /**
     * 业务场景解析器构造
     */
    public MatchGroupResolver() {
        super(CONFIG_KEY);
    }

    @Override
    protected Class<BusinessMatcher> getRuleClass() {
        return BusinessMatcher.class;
    }
}
