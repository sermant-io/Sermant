package com.huawei.apm.premain;

import com.lubanops.apm.bootstrap.log.LogFactory;
import com.lubanops.apm.bootstrap.utils.StringUtils;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.io.IOException;

/**
 * byte buddy增强监听器，用于保存增强后的字节码
 */
class LoadListener implements AgentBuilder.Listener {
    private final String exportPath = System.getProperty("apm.agent.class.export.path");
    @Override
    public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
    }

    @Override
    public void onTransformation(
            final TypeDescription typeDescription,
            final ClassLoader classLoader,
            final JavaModule module,
            final boolean loaded,
            final DynamicType dynamicType) {
        try {
            if (StringUtils.isNotBlank(exportPath)) {
                dynamicType.saveIn(new File(exportPath));
            }
        } catch (IOException e) {
            LogFactory.getLogger()
                    .warning(String.format("save class {%s} byte code failed", typeDescription.getTypeName()));
        }
    }

    @Override
    public void onIgnored(
            final TypeDescription typeDescription,
            final ClassLoader classLoader,
            final JavaModule module,
            final boolean loaded) {
    }

    @Override
    public void onError(
            final String typeName,
            final ClassLoader classLoader,
            final JavaModule module,
            final boolean loaded,
            final Throwable throwable) {
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
    }
}
