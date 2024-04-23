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

package io.sermant.core.operation.converter.api;

import io.sermant.core.operation.BaseOperation;

import java.io.Reader;
import java.util.Optional;

/**
 * Yaml Converter Interface
 *
 * @author luanwenfei
 * @since 2022-06-21
 */
public interface YamlConverter extends BaseOperation {
    /**
     * Convert Yaml to the target data type
     *
     * @param source data source
     * @param type target type
     * @param <T> data type class
     * @return target data object
     */
    <T> Optional<T> convert(String source, Class<? super T> type);

    /**
     * Convert Yaml to the target data type
     *
     * @param reader data source
     * @param type target type
     * @param <T> data type class
     * @return target data object
     */
    <T> Optional<T> convert(Reader reader,Class<? super T> type);

    /**
     * Convert Yaml to the target data type
     *
     * @param data data source
     * @return target data string
     */
    String dump(Object data);
}
