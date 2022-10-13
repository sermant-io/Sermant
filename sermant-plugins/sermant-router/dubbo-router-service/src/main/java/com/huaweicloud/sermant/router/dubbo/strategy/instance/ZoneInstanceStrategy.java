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

package com.huaweicloud.sermant.router.dubbo.strategy.instance;

import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.strategy.AbstractInstanceStrategy;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 匹配相同区域的invoker
 *
 * @author provenceee
 * @since 2022-09-27
 */
public class ZoneInstanceStrategy extends AbstractInstanceStrategy<Object, String> {
    /**
     * 匹配相同区域的invoker
     *
     * @param invoker Invoker
     * @param zone 区域
     * @param mapper 获取metadata的方法
     * @return 是否匹配
     */
    @Override
    public boolean isMatch(Object invoker, String zone, Function<Object, Map<String, String>> mapper) {
        return Objects.equals(getMetadata(invoker, mapper).get(RouterConstant.ZONE_KEY), zone);
    }
}