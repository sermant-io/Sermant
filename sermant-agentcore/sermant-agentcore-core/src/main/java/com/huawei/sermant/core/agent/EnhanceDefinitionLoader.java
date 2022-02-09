/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/skywalking/apm/agent/core/plugin/PluginBootstrap.java
 * and org/apache/skywalking/apm/agent/core/plugin/PluginFinder.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.agent;

import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.not;

import com.huawei.sermant.core.agent.annotations.AboutDelete;
import com.huawei.sermant.core.agent.common.VersionChecker;
import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.NameMatcher;
import com.huawei.sermant.core.agent.matcher.NonNameMatcher;
import com.huawei.sermant.core.lubanops.bootstrap.Listener;
import com.huawei.sermant.core.lubanops.bootstrap.NamedListener;
import com.huawei.sermant.core.lubanops.bootstrap.TransformerMethod;
import com.huawei.sermant.core.lubanops.bootstrap.utils.Util;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * 插件加载器 加载NamedListener以及EnhanceDefinition插件
 * <p> Copyright 2021
 *
 * @since 2021
 */
@AboutDelete
@Deprecated
public enum EnhanceDefinitionLoader {
    @SuppressWarnings({"checkstyle:JavadocVariable", "checkstyle:AnnotationLocation"})
    INSTANCE;

    private final Map<String, LinkedList<EnhanceDefinition>> nameDefinitions =
            new HashMap<String, LinkedList<EnhanceDefinition>>();

    private final List<EnhanceDefinition> nonNameDefinitions = new LinkedList<EnhanceDefinition>();

    /**
     * key : 增强类 value: 拦截器列表
     */
    private final Map<String, Listener> originMatchNamedListeners = new HashMap<String, Listener>();

    EnhanceDefinitionLoader() {
        load();
    }

    public static EnhanceDefinitionLoader getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("checkstyle:RegexpMultiline")
    public void load() {
        // 加载插件
        for (EnhanceDefinition definition : loadEnhanceDefinition()) {
            ClassMatcher classMatcher = definition.enhanceClass();
            if (classMatcher == null) {
                continue;
            }
            if (classMatcher instanceof NameMatcher) {
                String className = ((NameMatcher) classMatcher).getClassName();
                LinkedList<EnhanceDefinition> definitions = nameDefinitions.get(className);
                if (definitions == null) {
                    definitions = new LinkedList<EnhanceDefinition>();
                    nameDefinitions.put(className, definitions);
                }
                definitions.add(definition);
            } else {
                nonNameDefinitions.add(definition);
            }
        }
        // 原来的Listener
        for (Listener listener : loadNamedListener()) {
            resolveNamedListener(listener);
            initNamedListener(listener);
        }
    }

    private void initNamedListener(Listener listener) {
        listener.init();
    }

    private void resolveNamedListener(Listener listener) {
        String version = Util.getJarVersionFromProtectionDomain(listener.getClass().getProtectionDomain());
        if (!new VersionChecker(version, listener).check()) {
            return;
        }
        Set<String> classes = listener.getClasses();
        if (classes == null || classes.isEmpty()) {
            return;
        }
        for (String originClass : classes) {
            if (originClass == null || originClass.length() == 0) {
                continue;
            }
            originMatchNamedListeners.put(originClass.replace('/', '.'), new BufferedListener(listener));
        }
    }

    /**
     * 加载{@link NamedListener}
     *
     * @return SPI 列表
     */
    private Iterable<? extends Listener> loadNamedListener() {
        return ServiceLoader.load(Listener.class);
    }

    /**
     * 加载{@link EnhanceDefinition}
     *
     * @return SPI 列表
     */
    private Iterable<? extends EnhanceDefinition> loadEnhanceDefinition() {
        return ServiceLoader.load(EnhanceDefinition.class);
    }

    public ElementMatcher<TypeDescription> buildMatch() {
        ElementMatcher.Junction<TypeDescription> junction =
            new ElementMatcher.Junction.AbstractBase<TypeDescription>() {
                @Override
                public boolean matches(TypeDescription target) {
                    return nameDefinitions.containsKey(target.getActualName())
                            || originMatchNamedListeners.containsKey(target.getActualName());
                }
            };
        for (EnhanceDefinition nonNameDefinition : nonNameDefinitions) {
            junction = junction.or(((NonNameMatcher) nonNameDefinition.enhanceClass()).buildJunction());
        }
        return junction.and(not(isInterface()));
    }

    public List<EnhanceDefinition> findDefinitions(TypeDescription typeDescription) {
        LinkedList<EnhanceDefinition> matchDefinitions = nameDefinitions.get(typeDescription.getTypeName());
        if (matchDefinitions == null) {
            matchDefinitions = new LinkedList<EnhanceDefinition>();
        }
        for (EnhanceDefinition definition : nonNameDefinitions) {
            if (((NonNameMatcher) definition.enhanceClass()).isMatch(typeDescription)) {
                matchDefinitions.add(definition);
            }
        }
        return matchDefinitions;
    }

    public Listener findNameListener(TypeDescription typeDescription) {
        // 适配原始插件
        return originMatchNamedListeners.get(typeDescription.getTypeName());
    }

    private static class BufferedListener implements Listener {
        private final Listener listener;
        private volatile boolean initFlag = true;
        private volatile boolean addTagFlag = true;

        private BufferedListener(Listener listener) {
            this.listener = listener;
        }

        @Override
        public void init() {
            if (initFlag) {
                synchronized (listener) {
                    if (initFlag) {
                        listener.init();
                        initFlag = false;
                    }
                }
            }
        }

        @Override
        public Set<String> getClasses() {
            return listener.getClasses();
        }

        @Override
        public List<TransformerMethod> getTransformerMethod() {
            return listener.getTransformerMethod();
        }

        @Override
        public boolean hasAttribute() {
            return listener.hasAttribute();
        }

        @Override
        public List<String> getFields() {
            return listener.getFields();
        }

        @Override
        public void addTag() {
            if (addTagFlag) {
                synchronized (listener) {
                    if (addTagFlag) {
                        listener.addTag();
                        addTagFlag = false;
                    }
                }
            }
        }
    }
}
