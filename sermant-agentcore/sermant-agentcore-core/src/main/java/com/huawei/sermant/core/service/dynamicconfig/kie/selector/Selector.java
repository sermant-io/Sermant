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

package com.huawei.sermant.core.service.dynamicconfig.kie.selector;

import java.util.List;

/**
 * 选择器
 *
 * @author zhouss
 * @since 2021-11-17
 */
public interface Selector<R> {
    /**
     * 选择
     *
     * @param list 目标集合
     * @return 确定的目标
     */
    R select(List<R> list);

    /**
     * 选择
     *
     * @param strategy 选择策略
     * @param list 目标集合
     * @return 确定的目标
     */
    R select(List<R> list, SelectStrategy<R> strategy);
}
