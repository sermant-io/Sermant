package com.huawei.apm.premain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.apm.core.AgentCoreEntrance;
import com.huawei.apm.core.lubanops.bootstrap.commons.LubanApmConstants;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.bootstrap.log.LogPathUtils;
import com.huawei.apm.premain.common.BootArgsBuilder;
import com.huawei.apm.premain.common.PathDeclarer;
import com.huawei.apm.premain.exception.DupPremainException;
import com.huawei.apm.premain.exception.InitPremainException;
import com.huawei.apm.premain.lubanops.log.CollectorLogFactory;

public class AgentPremain {
    private static Logger logger;

    private static boolean executeFlag = false;

    //~~ premain method

    @SuppressWarnings("rawtypes")
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        try {
            // 执行标记，防止重复运行
            if (executeFlag) {
                throw new DupPremainException();
            }
            executeFlag = true;
            // 添加核心库
            loadCoreLib(instrumentation);
            // 初始化启动参数
            final Map<String, Object> argsMap = BootArgsBuilder.build(agentArgs);
            // 初始化日志
            initLog(argsMap);
            logger.info("[APM PREMAIN]loading javamesh agent.");
            // agent core入口
            AgentCoreEntrance.run(argsMap, instrumentation);
        } catch (Exception e) {
            if (logger == null) {
                throw new InitPremainException(e);
            } else {
                logger.log(Level.SEVERE, "[APM PREMAIN]Loading javamesh agent failed", e);
            }
        }

    }

    //~~internal methods

    private static void loadCoreLib(Instrumentation instrumentation) throws IOException {
        final File coreDir = new File(PathDeclarer.getCorePath());
        if (!coreDir.exists() || !coreDir.isDirectory()) {
            throw new FileNotFoundException(PathDeclarer.getCorePath() + " not found. ");
        }
        final File[] jars = coreDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        if (jars == null || jars.length <= 0) {
            throw new FileNotFoundException(PathDeclarer.getCorePath() + " has no core lib. ");
        }
        for (File jar : jars) {
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(jar));
        }
    }

    private static void initLog(Map<String, Object> args) {
        final String appName = args.get(LubanApmConstants.APP_NAME_COMMONS).toString();
        final String instanceName = args.get(LubanApmConstants.INSTANCE_NAME_COMMONS).toString();
        LogPathUtils.build(appName, instanceName);
        logger = CollectorLogFactory.getLogger("javamesh.apm");
        LogFactory.setLogger(logger);
    }
}
