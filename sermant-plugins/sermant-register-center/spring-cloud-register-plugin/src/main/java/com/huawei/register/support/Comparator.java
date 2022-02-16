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

package com.huawei.register.support;

/**
 * 比较器
 *
 * @param <T> 目标
 * @param <S> 源
 * @author zhouss
 * @since 2022-02-17
 */
public interface Comparator<S, T> {
    /**
     * 是否相同
     *
     * @param source 源
     * @param target 目标
     * @return 是否相同
     */
    boolean isSame(S source, T target);
}
