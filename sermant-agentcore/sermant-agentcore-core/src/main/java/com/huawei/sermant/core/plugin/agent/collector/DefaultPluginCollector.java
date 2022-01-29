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

package com.huawei.sermant.core.plugin.agent.collector;

import com.huawei.sermant.core.plugin.agent.declarer.PluginDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.PluginDescription;

import java.util.ServiceLoader;

/**
 * 默认的插件收集器，直接通过spi检索插件声明器和插件描述器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class DefaultPluginCollector implements PluginCollector {
    @Override
    public Iterable<? extends PluginDeclarer> getDeclarers() {
        return ServiceLoader.load(PluginDeclarer.class);
    }

    @Override
    public Iterable<? extends PluginDescription> getDescriptions() {
        return ServiceLoader.load(PluginDescription.class);
    }
}
