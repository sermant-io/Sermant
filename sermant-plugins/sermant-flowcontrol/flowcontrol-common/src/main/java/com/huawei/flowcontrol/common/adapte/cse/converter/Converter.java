/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.common.adapte.cse.converter;

/**
 * 转换器 当前用于转换相关规则
 *
 * @param <S> 源类型
 * @param <T> 目标类型
 * @author zhouss
 * @since 2021-11-16
 */
public interface Converter<S, T> {
    /**
     * 转换
     *
     * @param source 源数据类型
     * @return 目前数据
     */
    T convert(S source);
}
