/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core;

import java.lang.instrument.Instrumentation;
import java.util.Map;

import com.huawei.apm.core.agent.ByteBuddyAgentBuilder;
import com.huawei.apm.core.common.BootArgsIndexer;
import com.huawei.apm.core.common.LoggerFactory;
import com.huawei.apm.core.config.ConfigManager;
import com.huawei.apm.core.lubanops.core.BootStrapImpl;
import com.huawei.apm.core.plugin.PluginManager;
import com.huawei.apm.core.service.ServiceManager;

/**
 * agent core入口
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public class AgentCoreEntrance {
    /**
     * 入口方法
     *
     * @param argsMap         参数集
     * @param instrumentation Instrumentation对象
     * @throws Exception agent core执行异常
     */
    public static void run(Map<String, Object> argsMap, Instrumentation instrumentation) throws Exception {
        // 初始化日志
        LoggerFactory.init(argsMap);
        try {
            // 封装启动参数信息
            BootArgsIndexer.build(argsMap);
            // 初始化统一配置
            ConfigManager.initialize(argsMap);

            // 调用BootStrapImpl#main，启动luban核心功能
            BootStrapImpl.main(instrumentation, argsMap);

            // 启动核心服务
            ServiceManager.initServices();
            // 初始化插件
            PluginManager.initialize(instrumentation);
            // 初始化byte buddy
            ByteBuddyAgentBuilder.initialize(instrumentation);
        } finally {
            // 回滚日志
            LoggerFactory.rollback();
        }
    }
}
