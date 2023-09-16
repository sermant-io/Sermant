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
import com.huaweicloud.sermant.core.plugin.agent.template.DefaultAdviser;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.god.common.SermantManager;

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * agent core入口
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class AgentCoreEntrance {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 缓存当前Agent的类型，默认为premain方式启动
     */
    private static int agentType = AgentType.PREMAIN.getValue();

    /**
     * 缓存当前Agent的产品名
     */
    private static String artifactCache;

    /**
     * 缓存当前Agent的adviser
     */
    private static AdviserInterface adviserCache;

    private AgentCoreEntrance() {
    }

    /**
     * 入口方法
     *
     * @param artifact 产品名
     * @param argsMap 参数集
     * @param instrumentation Instrumentation对象
     * @param isDynamic 是否为动态安装 premain[false],agentmain[true]
     * @throws Exception agent core执行异常
     */
    public static void install(String artifact, Map<String, Object> argsMap, Instrumentation instrumentation,
            boolean isDynamic) throws Exception {
        if (isDynamic) {
            agentType = AgentType.AGENTMAIN.getValue();
        }
        artifactCache = artifact;
        adviserCache = new DefaultAdviser();

        // 初始化框架类加载器
        ClassLoaderManager.init(argsMap);

        // 初始化日志
        LoggerFactory.init();

        // 通过启动配置构建路径索引
        BootArgsIndexer.build(argsMap);

        // 初始化统一配置
        ConfigManager.initialize(argsMap);

        // 初始化操作类
        OperationManager.initOperations();

        // 启动核心服务
        ServiceManager.initServices();

        // 初始化事件系统
        EventManager.init();

        // 初始化ByteEnhanceManager
        ByteEnhanceManager.init(instrumentation);

        // 初始化插件
        PluginSystemEntrance.initialize(isDynamic);

        // 注册Adviser
        AdviserScheduler.registry(adviserCache);

        // 静态插件在全部加载结束后，统一增强，复用一个AgentBuilder
        if (!isDynamic) {
            ByteEnhanceManager.enhance();
        }

        // 上报Sermant启动事件
        FrameworkEventCollector.getInstance().collectAgentStartEvent();

        // 内部通知，Sermant启动完成通知
        if (NotificationManager.isEnable()) {
            NotificationManager.doNotify(new NotificationInfo(SermantNotificationType.LOAD_COMPLETE, null));
        }
    }

    /**
     * 卸载当前Sermant
     */
    public static void uninstall() {
        if (isPremain()) {
            LOGGER.log(Level.WARNING, "Sermant are not allowed to be uninstall when booting through premain.");
            return;
        }

        // 在Adviser调度器中取消注册当前Agent的Adviser
        AdviserScheduler.unRegistry(adviserCache);

        // 卸载全部的插件
        PluginManager.uninstallAll();

        // 关闭事件系统
        EventManager.shutdown();

        // 关闭所有服务
        ServiceManager.shutdown();

        // 清理操作类
        OperationManager.shutdown();

        // 清理配置类
        ConfigManager.shutdown();

        // 设置该artifact的Sermant状态为false，非运行状态
        SermantManager.updateSermantStatus(artifactCache, false);
    }

    /**
     * 该Sermant是否以premain方式启动
     *
     * @return boolean
     */
    public static boolean isPremain() {
        return AgentType.PREMAIN.getValue() == agentType;
    }
}
