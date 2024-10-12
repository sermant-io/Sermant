/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.config.strategy;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * Match instances that are not in mismatch
 *
 * @param <I> Instance generics
 * @param <T> Label generics
 * @author provenceee
 * @since 2021-12-08
 */
public abstract class AbstractInstanceStrategy<I, T> implements InstanceStrategy<I, T> {
    /**
     * Get metadata
     *
     * @param instance Instance
     * @param mapper Methods to obtain metadata
     * @return metadata
     */
    protected Map<String, String> getMetadata(I instance, Function<I, Map<String, String>> mapper) {
        if (mapper == null || instance == null) {
            return Collections.emptyMap();
        }
        return mapper.apply(instance);
    }
}
