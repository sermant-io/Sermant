package com.huawei.apm.core.agent;

import java.security.ProtectionDomain;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.utility.JavaModule;

import com.huawei.apm.core.classloader.PluginClassLoader;

class IgnoreClassMatcher implements AgentBuilder.RawMatcher {
    @Override
    public boolean matches(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
            Class<?> classBeingRedefined, ProtectionDomain protectionDomain) {
        final String typeName = typeDescription.getTypeName();
        if (typeName.startsWith("com.huawei.apm.bootstrap.") ||
                typeName.startsWith("com.huawei.apm.core.") ||
                typeName.startsWith("com.huawei.apm.core.ext.") ||
                typeName.startsWith("com.huawei.apm.premain.")) {
            return true;
        }
        return classLoader != null && classLoader.getClass() == PluginClassLoader.class;
    }
}
