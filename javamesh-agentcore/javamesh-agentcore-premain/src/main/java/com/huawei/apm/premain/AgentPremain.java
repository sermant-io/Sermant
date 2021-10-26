package com.huawei.apm.premain;

import com.huawei.apm.bootstrap.boot.CoreServiceManager;
import com.huawei.apm.bootstrap.config.ConfigLoader;
import com.huawei.apm.bootstrap.serialize.SerializerHolder;
import com.huawei.apm.premain.classloader.ClassLoaderManager;
import com.huawei.apm.premain.classloader.PluginClassLoader;
import com.huawei.apm.premain.agent.BootstrapEnhance;
import com.huawei.apm.premain.agent.ByteBuddyAgentBuilder;
import com.huawei.apm.premain.agent.NoneNamedListenerBuilder;
import com.huawei.apm.bootstrap.lubanops.commons.LubanApmConstants;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.lubanops.log.LogPathUtils;
import com.huawei.apm.premain.lubanops.agent.AgentStatus;
import com.huawei.apm.premain.lubanops.agent.ArgumentBuilder;
import com.huawei.apm.premain.lubanops.log.CollectorLogFactory;
import com.huawei.apm.premain.lubanops.utils.LibPathUtils;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentPremain {

    private static final String AGENT_JAR_FILE_NAME = "javamesh-agent.jar";

    private static AgentStatus agentStatus = AgentStatus.STOPPED;

    private static Logger logger;

    private static ArgumentBuilder argumentBuilder = new ArgumentBuilder();

    //~~ premain method

    @SuppressWarnings("rawtypes")
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        try {
            if (AgentStatus.STOPPED.equals(agentStatus)) {
                agentStatus = AgentStatus.LOADING;
                // 添加bootstrap包到bootstrap classloader中
                loadBootstrap(instrumentation);
                // 解析入参
                Map argsMap = argumentBuilder.build(agentArgs, new LogInitCallback());
                // 添加core和ext
                loadCoreLib(instrumentation);
                // 获取javaagent依赖的包
                logger.info("[APM PREMAIN]loading javaagent.");
                // 初始化序列化器
                SerializerHolder.initialize(PluginClassLoader.getDefault());
                ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
                // 配置初始化
                ConfigLoader.initialize(agentArgs, ClassLoaderManager.getTargetClassLoader(currentClassLoader));

                // 反射调用BootStrapImpl#main
                addAgentPath(argsMap);
                final Class<?> mainClass = currentClassLoader.loadClass("com.huawei.apm.core.lubanops.BootStrapImpl");
                if (mainClass != null) {
                    Method startMethod = mainClass.getDeclaredMethod("main", Instrumentation.class, Map.class);
                    startMethod.invoke(null, instrumentation, argsMap);
                }

                // 启动核心服务
                CoreServiceManager.INSTANCE.initServices();
                // 针对NoneNamedListener初始化增强
                NoneNamedListenerBuilder.initialize(instrumentation);
                // 初始化byte buddy
                ByteBuddyAgentBuilder.initialize(instrumentation);
                // 重定义, 使之可被bytebuddy增强
                BootstrapEnhance.reTransformClasses(instrumentation);
            } else {
                logger.log(Level.SEVERE, "[APM PREMAIN]The JavaAgent is loaded repeatedly.");
            }
            AgentPremain.agentStatus = AgentStatus.STARTED;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[APM PREMAIN]Loading javaagent failed", e);
        }

    }

    private static void loadCoreLib(Instrumentation instrumentation) throws IOException {
        final List<URL> urls = LibPathUtils.getLibUrl();
        for (URL url : urls) {
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(url.getPath()));
        }
    }

    //~~internal methods

    private static void addAgentPath(Map argsMap) {
        String agentPath = LibPathUtils.getAgentPath();
        String bootPath = LibPathUtils.getBootstrapJarPath();
        String pluginsPath = LibPathUtils.getPluginsPath() + File.separatorChar + LibPathUtils.getLubanOpsDirName();
        argsMap.put(LubanApmConstants.AGENT_PATH_COMMONS, agentPath);
        argsMap.put(LubanApmConstants.BOOT_PATH_COMMONS, bootPath);
        argsMap.put(LubanApmConstants.PLUGINS_PATH_COMMONS, pluginsPath);
    }

    private static void loadBootstrap(Instrumentation instrumentation) throws IOException {
        ProtectionDomain pd = AgentPremain.class.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        String jarPath = cs.getLocation().getPath();
        String agentPath = jarPath.substring(0, jarPath.lastIndexOf(AGENT_JAR_FILE_NAME));
        String bootPath = agentPath + File.separator + "boot";
        List<JarFile> bootstrapJar = new ArrayList<JarFile>();
        File libDir = new File(bootPath);
        File[] files = libDir.listFiles();
        if (files != null) {
            for (File file : files) {
                bootstrapJar.add(new JarFile(file));
            }
        }
        for (JarFile jar : bootstrapJar) {
            instrumentation.appendToBootstrapClassLoaderSearch(jar);
        }
    }

    public static class LogInitCallback {
        public void initLog(String appName, String instanceName) {
            LogPathUtils.build(appName, instanceName);
            Logger apmLogger = CollectorLogFactory.getLogger("luban.apm");
            AgentPremain.logger = apmLogger;
            LogFactory.setLogger(apmLogger);
        }
    }
}
