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

package com.huaweicloud.sermant.core;

import com.huaweicloud.sermant.core.classloader.ClassLoaderManager;
import com.huaweicloud.sermant.core.common.AgentType;
import com.huaweicloud.sermant.core.common.BootArgsIndexer;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.event.EventManager;
import com.huaweicloud.sermant.core.event.collector.FrameworkEventCollector;
import com.huaweicloud.sermant.core.notification.NotificationInfo;
import com.huaweicloud.sermant.core.notification.NotificationManager;
import com.huaweicloud.sermant.core.notification.SermantNotificationType;
import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.plugin.PluginManager;
import com.huaweicloud.sermant.core.plugin.PluginSystemEntrance;
import com.huaweicloud.sermant.core.plugin.agent.ByteEnhanceManager;
import com.huaweicloud.sermant.core.plugin.agent.adviser.AdviserInterface;
import com.huaweicloud.sermant.core.plugin.agent.adviser.AdviserScheduler;
import com.huaweicloud.sermant.core.plugin.agent.info.EnhancementManager;
import com.huaweicloud.sermant.core.plugin.agent.template.DefaultAdviser;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.god.common.SermantManager;

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * agent core entrance
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class AgentCoreEntrance {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Cache the type of the current Agent. The default startup mode is premain
     */
    private static int agentType = AgentType.PREMAIN.getValue();

    /**
     * Cache the artifact name of the current Agent
     */
    private static String artifactCache;

    /**
     * Cache the current Agent adviser
     */
    private static AdviserInterface adviserCache;

    private AgentCoreEntrance() {
    }

    /**
     * Entry method
     *
     * @param artifact artifact name
     * @param argsMap argsMap
     * @param instrumentation instrumentation object
     * @param isDynamic is Dynamic installation, premain[false],agentmain[true]
     * @throws Exception agent core execution exception
     */
    public static void install(String artifact, Map<String, Object> argsMap, Instrumentation instrumentation,
            boolean isDynamic) throws Exception {
        if (isDynamic) {
            agentType = AgentType.AGENTMAIN.getValue();
        }
        artifactCache = artifact;
        adviserCache = new DefaultAdviser();

        // Initialize default logs to ensure log availability before loading the log engine
        LoggerFactory.initDefaultLogger(artifact);

        // Initialize the classloader of framework
        ClassLoaderManager.init(argsMap);

        // Initialize LoggerFactory for adding SermantBridgeHandler
        LoggerFactory.init(artifact);

        // Build the path index by startup configuration
        BootArgsIndexer.build(argsMap);

        // Initialize the unified configuration
        ConfigManager.initialize(argsMap);

        // Initialize the operation class
        OperationManager.initOperations();

        // Start core services
        ServiceManager.initServices();

        // Initialize the event system
        EventManager.init();

        // Initialize ByteEnhanceManager
        ByteEnhanceManager.init(instrumentation);

        // Initialize plugins
        PluginSystemEntrance.initialize(isDynamic);

        // Registered Adviser
        AdviserScheduler.registry(adviserCache);

        // After all static plugins are loaded, they are enhanced in a unified manner, using one AgentBuilder
        if (!isDynamic) {
            ByteEnhanceManager.enhance();
        }

        // Report Sermant start event
        FrameworkEventCollector.getInstance().collectAgentStartEvent();

        // Internal notification, Sermant start-up completion notification
        if (NotificationManager.isEnable()) {
            NotificationManager.doNotify(new NotificationInfo(SermantNotificationType.LOAD_COMPLETE, null));
        }
    }

    /**
     * Uninstall current Sermant
     */
    public static void uninstall() {
        if (isPremain()) {
            LOGGER.log(Level.WARNING, "Sermant are not allowed to be uninstall when booting through premain.");
            return;
        }

        // Unregister the Adviser of the current Agent in AdviserScheduler
        AdviserScheduler.unRegistry(adviserCache);

        // Uninstall all plugins
        PluginManager.uninstallAll();

        // Close event system
        EventManager.shutdown();

        // Shut down all services
        ServiceManager.shutdown();

        // Cleanup operation class
        OperationManager.shutdown();

        // Cleanup configuration class
        ConfigManager.shutdown();

        // Cleanup the enhanced class information
        EnhancementManager.shutdown();

        // Set the Sermant state of this artifact to false, not running
        SermantManager.updateSermantStatus(artifactCache, false);
    }

    /**
     * Whether the Sermant starts in premain mode
     *
     * @return boolean
     */
    public static boolean isPremain() {
        return AgentType.PREMAIN.getValue() == agentType;
    }
}
