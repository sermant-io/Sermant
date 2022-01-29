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

/**
 * 插件收集器，{@link PluginDeclarer}和{@link PluginDescription}采集的低阶api
 * <p>{@link PluginDeclarer}和{@link PluginDescription}的采集方式有两种：
 * <pre>
 *     1.高阶api，直接定义{@link PluginDeclarer}和{@link PluginDescription}的spi文件
 *     2.低阶api，定义{@link PluginCollector}的配置文件，从{@link PluginCollector}中获取
 * </pre>
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-25
 */
public interface PluginCollector {
    /**
     * 获取插件声明器的集合
     *
     * @return 插件声明器的集合
     */
    Iterable<? extends PluginDeclarer> getDeclarers();

    /**
     * 获取插件描述器的集合
     *
     * @return 描述器的集合
     */
    Iterable<? extends PluginDescription> getDescriptions();
}
