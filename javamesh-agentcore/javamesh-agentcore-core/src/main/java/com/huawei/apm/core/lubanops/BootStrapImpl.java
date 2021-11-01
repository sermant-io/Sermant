package com.huawei.apm.core.lubanops;

import com.huawei.apm.bootstrap.lubanops.agent.AgentInfo;
import com.huawei.apm.bootstrap.lubanops.api.APIService;
import com.huawei.apm.bootstrap.lubanops.api.InstrumentationManager;
import com.huawei.apm.bootstrap.lubanops.config.AgentConfigManager;
import com.huawei.apm.bootstrap.lubanops.config.IdentityConfigManager;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.lubanops.utils.AgentUtils;
import com.huawei.apm.core.lubanops.api.JSONImpl;
import com.huawei.apm.core.lubanops.container.AgentServiceContainer;
import com.huawei.apm.core.lubanops.update.UpdateThread;
import com.huawei.apm.core.lubanops.utils.AgentPath;

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
            APIService.setJsonApi(new JSONImpl());

            // 设置javaagent启动时间和版本信息
            AgentInfo.setAgentStartTime(System.currentTimeMillis());
            String path = BootStrapImpl.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            String javaagentVersion = path.substring(
                    path.lastIndexOf("javamesh-agentcore-core-") + "javamesh-agentcore-core-".length(),
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
