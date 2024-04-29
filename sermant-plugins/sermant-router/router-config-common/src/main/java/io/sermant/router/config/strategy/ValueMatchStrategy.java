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

import java.util.List;

/**
 * Value matching strategy
 *
 * @author provenceee
 * @since 2021-10-14
 */
public interface ValueMatchStrategy {
    /**
     * Whether it matches or not
     *
     * @param values Expectations
     * @param arg Parameter value
     * @return Whether it matches or not
     */
    boolean isMatch(List<String> values, String arg);
}