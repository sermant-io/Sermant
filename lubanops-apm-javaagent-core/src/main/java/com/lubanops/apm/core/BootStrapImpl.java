package com.lubanops.apm.core;

import com.lubanops.apm.bootstrap.agent.AgentInfo;
import com.lubanops.apm.bootstrap.api.InstrumentationManager;
import com.lubanops.apm.bootstrap.config.AgentConfigManager;
import com.lubanops.apm.bootstrap.config.IdentityConfigManager;
import com.lubanops.apm.bootstrap.log.LogFactory;
import com.lubanops.apm.bootstrap.utils.AgentUtils;
import com.lubanops.apm.core.container.AgentServiceContainer;
import com.lubanops.apm.core.update.UpdateThread;
import com.lubanops.apm.core.utils.AgentPath;

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * javaagent启动类
 */
public class BootStrapImpl {
    private final static Logger LOGGER = LogFactory.getLogger();
    private static AtomicBoolean started = new AtomicBoolean(false);
    private static AtomicBoolean stopped = new AtomicBoolean(false);
    private AgentServiceContainer serviceContainer;

    public BootStrapImpl() {
        serviceContainer = new AgentServiceContainer();
    }

    public static void main(Instrumentation instrumentation, Map argsMap) {
        try {
            // 读取agent配置文件
            AgentPath agentPath = AgentPath.build(argsMap);
            AgentUtils.setInstrumentation(instrumentation);
            AgentConfigManager.init(agentPath.getAgentPath());
            // 设置身份信息
            IdentityConfigManager.init(argsMap, agentPath.getAgentPath());
            LOGGER.info("----------------------javaagent starting----------------------");
            InstrumentationManager.inst = instrumentation;

            // 设置javaagent启动时间和版本信息
            AgentInfo.setAgentStartTime(System.currentTimeMillis());
            String path = BootStrapImpl.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            String javaagentVersion = path.substring(
                    path.lastIndexOf("lubanops-apm-javaagent-core-") + "lubanops-apm-javaagent-core-".length(),
                    path.lastIndexOf(".jar"));
            AgentInfo.setJavaagentVersion(javaagentVersion);
            LOGGER.info("javaagentVersion:" + javaagentVersion);
            new BootStrapImpl().start();
            UpdateThread.getInstance().start();
            LOGGER.info("----------------------javaagent started----------------------");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "----------------------javaagent start failed----------------------");
        }
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            addShutdownHook();
            serviceContainer.start();
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopAgent();
            }
        });
    }

    public void stopAgent() {
        if (stopped.compareAndSet(false, true)) {
            try {
                LOGGER.info("HeartBeatTaskThread stopped!!!");
                serviceContainer.stop();
                LOGGER.info("HarvestTaskThread stopped!!!");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "error occurred when stopping collector:", e);
            }
        }
    }
}
