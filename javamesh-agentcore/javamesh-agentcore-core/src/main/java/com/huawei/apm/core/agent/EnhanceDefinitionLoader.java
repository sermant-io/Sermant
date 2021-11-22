package com.huawei.apm.core.agent;

import com.huawei.apm.core.agent.common.VersionChecker;
import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.lubanops.bootstrap.NamedListener;
import com.huawei.apm.core.lubanops.bootstrap.TransformerMethod;
import com.huawei.apm.core.lubanops.bootstrap.utils.Util;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.NameMatcher;
import com.huawei.apm.core.agent.matcher.NonNameMatcher;
import com.huawei.apm.core.lubanops.bootstrap.Listener;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

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
public enum EnhanceDefinitionLoader {
    INSTANCE;

    private final Map<String, LinkedList<EnhanceDefinition>> nameDefinitions =
        new HashMap<String, LinkedList<EnhanceDefinition>>();

    private final List<EnhanceDefinition> nonNameDefinitions = new LinkedList<EnhanceDefinition>();

    /**
     * key : 增强类
     * value: 拦截器列表
     */
    private final Map<String, Listener> originMatchNamedListeners = new HashMap<String, Listener>();

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

    public static EnhanceDefinitionLoader getInstance() {
        return INSTANCE;
    }

    private static class BufferedListener implements Listener {
        private volatile boolean initFlag = true;
        private volatile boolean addTagFlag = true;
        private final Listener listener;

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
