package com.huawei.apm.premain;

import com.huawei.apm.core.service.CoreServiceManager;
import com.huawei.apm.core.config.ConfigLoader;
import com.huawei.apm.core.lubanops.bootstrap.commons.LubanApmConstants;
import com.huawei.apm.core.lubanops.core.BootStrapImpl;
import com.huawei.apm.core.common.PathIndexer;
import com.huawei.apm.core.serialize.SerializerHolder;
import com.huawei.apm.core.agent.ByteBuddyAgentBuilder;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.bootstrap.log.LogPathUtils;
import com.huawei.apm.premain.lubanops.agent.AgentStatus;
import com.huawei.apm.premain.lubanops.agent.ArgumentBuilder;
import com.huawei.apm.premain.lubanops.log.CollectorLogFactory;
import com.huawei.apm.premain.lubanops.utils.LibPathUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentPremain {
    private static AgentStatus agentStatus = AgentStatus.STOPPED;

    private static Logger logger;

    private static ArgumentBuilder argumentBuilder = new ArgumentBuilder();

    //~~ premain method

    @SuppressWarnings("rawtypes")
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        try {
            if (AgentStatus.STOPPED.equals(agentStatus)) {
                agentStatus = AgentStatus.LOADING;
                // 添加core
                loadCoreLib(instrumentation);
                // 初始化参数
                Map argsMap = argumentBuilder.build(agentArgs, new LogInitCallback());
                addAgentPath(argsMap);

                logger.info("[APM PREMAIN]loading javamesh agent.");

                // 初始化路径
                PathIndexer.build(LibPathUtils.getConfigPath(), LibPathUtils.getPluginsPath(),
                        Collections.singleton(LibPathUtils.getLubanOpsDirName()));
                // 初始化序列化器
                SerializerHolder.initialize();
                // 初始化统一配置
                ConfigLoader.initialize(agentArgs);

                // 调用BootStrapImpl#main，启动luban核心功能
                BootStrapImpl.main(instrumentation, argsMap);

                // 启动核心服务
                CoreServiceManager.INSTANCE.initServices();
                // 初始化byte buddy
                ByteBuddyAgentBuilder.initialize(instrumentation);
            } else {
                logger.log(Level.SEVERE, "[APM PREMAIN]Javamesh agent has already been loaded.");
            }
            AgentPremain.agentStatus = AgentStatus.STARTED;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[APM PREMAIN]Loading javamesh agent failed", e);
        }

    }

    //~~internal methods

    private static void addAgentPath(Map argsMap) {
        argsMap.put(LubanApmConstants.AGENT_PATH_COMMONS, LibPathUtils.getAgentPath());
        argsMap.put(LubanApmConstants.BOOT_PATH_COMMONS, LibPathUtils.getCorePath());
        argsMap.put(LubanApmConstants.PLUGINS_PATH_COMMONS, LibPathUtils.getLubanOpsPluginsPath());
    }

    private static void loadCoreLib(Instrumentation instrumentation) throws IOException {
        final File coreDir = new File(LibPathUtils.getCorePath());
        if (coreDir.exists() && coreDir.isDirectory()) {
            final File[] jars = coreDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });
            if (jars != null) {
                for (File jar : jars) {
                    instrumentation.appendToSystemClassLoaderSearch(new JarFile(jar));
                }
            }
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
