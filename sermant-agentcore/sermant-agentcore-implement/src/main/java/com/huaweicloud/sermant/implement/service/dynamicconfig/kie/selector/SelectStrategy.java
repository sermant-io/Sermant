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
import java.util.Random;

/**
 * Selection strategy
 *
 * @param <R> 泛型
 * @author zhouss
 * @since 2021-11-17
 */
public interface SelectStrategy<R> {
    /**
     * select
     *
     * @param list target list
     * @return selected target
     */
    R select(List<R> list);

    /**
     * Polling strategy
     *
     * @param <R> collection type
     * @since 2021-11-17
     */
    class RoundStrategy<R> implements SelectStrategy<R> {
        private int index = 0;

        @Override
        public R select(List<R> list) {
            if (list == null || list.isEmpty()) {
                return null;
            }
            index++;
            int selectIndex = Math.abs(index) % list.size();
            if (index == Integer.MAX_VALUE) {
                index = 0;
            }
            return list.get(selectIndex);
        }
    }

    /**
     * Random selection strategy
     *
     * @param <R> collection type
     * @since 2021-11-17
     */
    class RandomStrategy<R> implements SelectStrategy<R> {
        private final Random random = new Random();

        @Override
        public R select(List<R> list) {
            if (list == null || list.isEmpty()) {
                return null;
            }
            return list.get(random.nextInt(list.size()));
        }
    }
}
