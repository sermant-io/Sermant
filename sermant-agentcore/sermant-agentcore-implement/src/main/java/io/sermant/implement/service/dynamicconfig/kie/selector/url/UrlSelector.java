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

package io.sermant.implement.service.dynamicconfig.kie.selector.url;

import io.sermant.implement.service.dynamicconfig.kie.selector.SelectStrategy;
import io.sermant.implement.service.dynamicconfig.kie.selector.Selector;

import java.util.List;

/**
 * url
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class UrlSelector implements Selector<String> {

    private final SelectStrategy<String> defaultStrategy = new SelectStrategy.RoundStrategy<String>();

    @Override
    public String select(List<String> list) {
        return defaultStrategy.select(list);
    }

    @Override
    public String select(List<String> list, SelectStrategy<String> strategy) {
        return strategy.select(list);
    }
}
