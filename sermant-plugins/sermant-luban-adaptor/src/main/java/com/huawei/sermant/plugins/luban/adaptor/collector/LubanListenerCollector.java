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

package com.huawei.sermant.plugins.luban.adaptor.collector;

import com.huawei.sermant.core.plugin.adaptor.collector.AdaptorCollector;
import com.huawei.sermant.core.plugin.agent.collector.AbstractPluginCollector;
import com.huawei.sermant.core.plugin.agent.declarer.PluginDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.PluginDescription;
import com.huawei.sermant.plugins.luban.adaptor.declarer.LubanPluginDeclarer;
import com.huawei.sermant.plugins.luban.adaptor.declarer.LubanPluginDescription;

import com.lubanops.apm.bootstrap.Listener;
import com.lubanops.apm.bootstrap.NoneNamedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * luban的监听器收集器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class LubanListenerCollector extends AbstractPluginCollector implements AdaptorCollector {
    /**
     * 加载luban插件的类加载器
     */
    private static ClassLoader pluginClassLoader;

    /**
     * luban的监听器集合
     */
    private static Iterable<Listener> listeners = Collections.emptyList();

    /**
     * luban的非命名监听器集合
     */
    private static Iterable<NoneNamedListener> noneNamedListeners = Collections.emptyList();

    /**
     * 执行初始化，加载监听器和非命名监听器并初始化他们
     *
     * @param classLoader 加载luban插件的类加载器
     */
    public static void initialize(ClassLoader classLoader) {
        pluginClassLoader = classLoader;
        listeners = ServiceLoader.load(Listener.class, classLoader);
        for (Listener listener : listeners) {
            listener.init();
        }
        noneNamedListeners = ServiceLoader.load(NoneNamedListener.class, classLoader);
        for (NoneNamedListener noneNamedListener : noneNamedListeners) {
            noneNamedListener.init();
        }
    }

    @Override
    public Iterable<? extends PluginDeclarer> getDeclarers() {
        final List<PluginDeclarer> declarers = new ArrayList<PluginDeclarer>();
        for (Listener listener : listeners) {
            listener.addTag();
            final Set<String> classes = listener.getClasses();
            if (classes == null || classes.isEmpty()) {
                continue;
            }
            declarers.add(new LubanPluginDeclarer(listener, pluginClassLoader));
        }
        return declarers;
    }

    @Override
    public Iterable<? extends PluginDescription> getDescriptions() {
        final List<PluginDescription> descriptions = new ArrayList<>();
        for (NoneNamedListener noneNamedListener : noneNamedListeners) {
            descriptions.add(new LubanPluginDescription(noneNamedListener, pluginClassLoader));
        }
        return descriptions;
    }
}
