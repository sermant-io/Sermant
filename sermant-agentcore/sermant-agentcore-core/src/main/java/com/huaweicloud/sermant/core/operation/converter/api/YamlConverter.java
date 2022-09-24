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

package com.huaweicloud.sermant.core.operation.converter.api;

import com.huaweicloud.sermant.core.operation.BaseOperation;

import java.io.Reader;
import java.util.Optional;

/**
 * Yaml转换器接口
 *
 * @author luanwenfei
 * @since 2022-06-21
 */
public interface YamlConverter extends BaseOperation {
    /**
     * 将Yaml转换为目标数据类型
     *
     * @param source 源数据
     * @param type 目标数据类型
     * @param <T> 目标数据类型
     * @return 目标数据
     */
    <T> Optional<T> convert(String source, Class<? super T> type);

    /**
     * 将Yaml转换为目标数据类型
     *
     * @param reader 源数据
     * @param type 目标数据类型
     * @param <T> 目标数据类型
     * @return 目标数据
     */
    <T> Optional<T> convert(Reader reader,Class<? super T> type);

    /**
     * 将数据转换为Yaml
     *
     * @param data 源数据
     * @return 转换后的数据
     */
    String dump(Object data);
}
