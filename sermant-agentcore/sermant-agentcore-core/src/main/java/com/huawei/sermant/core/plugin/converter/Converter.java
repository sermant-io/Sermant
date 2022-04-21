/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.plugin.converter;

import java.util.Optional;

/**
 * 转换器
 *
 * @param <T> 目标类型
 * @param <S> 源数据类型
 * @author zhouss
 * @since 2022-04-14
 */
public interface Converter<S, T> {
    /**
     * 转换为目标类型
     *
     * @param source 源数据
     * @return 目标类型数据
     */
    Optional<T> convert(S source);
}
