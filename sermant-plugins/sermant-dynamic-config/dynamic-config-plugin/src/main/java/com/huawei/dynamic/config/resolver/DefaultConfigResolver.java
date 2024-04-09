/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config.resolver;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.utils.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * the parser is configured by default
 *
 * @author zhouss
 * @since 2022-04-13
 */
public class DefaultConfigResolver implements ConfigResolver<Map<String, Object>> {
    private final YamlConverter yamlConverter = OperationManager.getOperation(YamlConverter.class);

    @Override
    public Map<String, Object> resolve(DynamicConfigEvent event) {
        final Map<String, Object> result = new HashMap<>();
        final Optional<Map<String, Object>> convert = yamlConverter.convert(event.getContent(), Map.class);
        convert.ifPresent(stringObjectMap -> MapUtils.resolveNestMap(result, stringObjectMap, null));
        return result;
    }
}
