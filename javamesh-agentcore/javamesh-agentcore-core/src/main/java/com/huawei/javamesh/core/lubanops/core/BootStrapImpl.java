/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.javamesh.core.lubanops.core;

import com.huawei.javamesh.core.common.BootArgsIndexer;
import com.huawei.javamesh.core.lubanops.bootstrap.agent.AgentInfo;
import com.huawei.javamesh.core.lubanops.bootstrap.api.APIService;
import com.huawei.javamesh.core.lubanops.bootstrap.api.InstrumentationManager;
import com.huawei.javamesh.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.config.IdentityConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.AgentUtils;
import com.huawei.javamesh.core.lubanops.core.api.JSONImpl;
import com.huawei.javamesh.core.lubanops.core.container.AgentServiceContainer;
import com.huawei.javamesh.core.lubanops.core.update.UpdateThread;
import com.huawei.javamesh.core.lubanops.core.utils.AgentPath;

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
            String javaagentVersion = BootArgsIndexer.getCoreVersion();
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
