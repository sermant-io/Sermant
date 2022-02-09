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

package com.huawei.sermant.core.plugin.adaptor.collector;

import com.huawei.sermant.core.plugin.agent.collector.AbstractPluginCollector;
import com.huawei.sermant.core.plugin.agent.declarer.PluginDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.PluginDescription;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 适配器插件收集器管理器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class AdaptorCollectorManager {
    private AdaptorCollectorManager() {
    }

    /**
     * 加载所有适配器插件收集器
     *
     * @param classLoader 加载适配包的类加载器
     */
    public static void loadCollectors(ClassLoader classLoader) {
        for (AdaptorCollector collector : ServiceLoader.load(AdaptorCollector.class, classLoader)) {
            final Class<? extends AdaptorCollector> cls = collector.getClass();
            if (!AdaptorPluginCollector.COLLECTORS.containsKey(cls)) {
                AdaptorPluginCollector.COLLECTORS.put(cls, collector);
            }
        }
    }

    /**
     * 适配器插件收集器的汇总实现
     */
    public static class AdaptorPluginCollector extends AbstractPluginCollector {
        /**
         * 适配器插件收集器
         */
        private static final Map<Class<?>, AdaptorCollector> COLLECTORS = new LinkedHashMap<>();

        @Override
        public Iterable<? extends PluginDeclarer> getDeclarers() {
            final List<PluginDeclarer> result = new ArrayList<>();
            for (AdaptorCollector collector : COLLECTORS.values()) {
                for (PluginDeclarer declarer : collector.getDeclarers()) {
                    result.add(declarer);
                }
            }
            return result;
        }

        @Override
        public Iterable<? extends PluginDescription> getDescriptions() {
            final List<PluginDescription> result = new ArrayList<>();
            for (AdaptorCollector collector : COLLECTORS.values()) {
                for (PluginDescription description : collector.getDescriptions()) {
                    result.add(description);
                }
            }
            return result;
        }
    }
}
