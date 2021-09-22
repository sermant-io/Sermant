package com.huawei.apm.premain;

import java.lang.instrument.Instrumentation;

/**
 * 启动增强类
 * 应用于对启动即加载的类增强
 * 原理是通过redefine的方法对字节码进行重新生成
 */
public class BootstrapEnhance {
    /**
     * 启动类加载类重定义
     */
    public static void reTransformClasses(Instrumentation instrumentation) {
        reTransformClass(instrumentation, "org.apache.logging.log4j.spi.AbstractLogger");
        reTransformClass(instrumentation, "ch.qos.logback.classic.Logger");
        reTransformClass(instrumentation, "java.util.concurrent.ThreadPoolExecutor");
        reTransformClass(instrumentation, "java.util.concurrent.ForkJoinPool");
        reTransformClass(instrumentation, "java.lang.Thread");
        reTransformClass(instrumentation, "org.apache.dubbo.registry.integration.RegistryProtocol");
    }

    private static void reTransformClass(Instrumentation inst, String name) {
        try {
            Class<?> reTransformClass = Class.forName(name);
            inst.retransformClasses(reTransformClass);
        } catch (Throwable e) {
        }
    }
}
