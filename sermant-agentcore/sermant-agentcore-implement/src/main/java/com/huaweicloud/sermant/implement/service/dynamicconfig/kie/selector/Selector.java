/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.implement.service.dynamicconfig.kie.selector;

import java.util.List;

/**
 * Selector
 *
 * @param <R> Generics
 * @author zhouss
 * @since 2021-11-17
 */
public interface Selector<R> {
    /**
     * select
     *
     * @param list target list
     * @return selected target
     */
    R select(List<R> list);

    /**
     * select
     *
     * @param strategy strategy for selection
     * @param list target list
     * @return selected target
     */
    R select(List<R> list, SelectStrategy<R> strategy);
}
