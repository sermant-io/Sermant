package com.huawei.apm.premain;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.NameMatcher;
import com.huawei.apm.bootstrap.matcher.NonNameMatcher;
import com.lubanops.apm.bootstrap.Listener;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * 插件加载器
 * 加载NamedListener以及EnhanceDefinition插件
 */
enum EnhanceDefinitionLoader {
    INSTANCE;

    private final Map<String, LinkedList<EnhanceDefinition>> nameDefinitions =
        new HashMap<String, LinkedList<EnhanceDefinition>>();

    private final List<EnhanceDefinition> nonNameDefinitions = new LinkedList<EnhanceDefinition>();

    /**
     * key : 增强类
     * value: 拦截器列表
     */
    private final Map<String, LinkedList<Listener>> originMatchNamedListeners =
        new HashMap<String, LinkedList<Listener>>();

    EnhanceDefinitionLoader() {
        load();
    }

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
        Set<String> classes = listener.getClasses();
        if (classes == null || classes.size() == 0) {
            return;
        }
        for (String originClass : classes) {
            if (originClass == null || originClass.length() == 0) {
                return;
            }
            final String replacedClass = originClass.replace('/', '.');
            LinkedList<Listener> topListeners = originMatchNamedListeners.get(replacedClass);
            if (topListeners == null) {
                topListeners = new LinkedList<Listener>();
                originMatchNamedListeners.put(replacedClass, topListeners);
            }
            topListeners.add(listener);
        }
    }

    /**
     * 加载{@link com.lubanops.apm.bootstrap.NamedListener}
     *
     * @return SPI 列表
     */
    private ServiceLoader<? extends Listener> loadNamedListener() {
        return AgentSpiLoader.load(Listener.class);
    }

    /**
     * 加载{@link com.huawei.apm.bootstrap.definition.EnhanceDefinition}
     *
     * @return SPI 列表
     */
    private ServiceLoader<? extends EnhanceDefinition> loadEnhanceDefinition() {
        return AgentSpiLoader.load(EnhanceDefinition.class);
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

    public List<Listener> findNameListeners(TypeDescription typeDescription) {
        // 适配原始插件
        LinkedList<Listener> originMatchListeners = originMatchNamedListeners.get(typeDescription.getTypeName());
        if (originMatchListeners == null) {
            return Collections.emptyList();
        }
        return originMatchListeners;
    }
}
