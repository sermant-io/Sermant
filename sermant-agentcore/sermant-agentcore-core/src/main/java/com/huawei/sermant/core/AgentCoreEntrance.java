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

package com.huawei.sermant.core;

import com.huawei.sermant.core.common.BootArgsIndexer;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.lubanops.core.BootStrapImpl;
import com.huawei.sermant.core.plugin.PluginSystemEntrance;
import com.huawei.sermant.core.service.ServiceManager;

import java.lang.instrument.Instrumentation;
import java.util.Map;

/**
 * agent core入口
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class AgentCoreEntrance {
    private AgentCoreEntrance() {
    }

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
            PluginSystemEntrance.initialize(instrumentation);
        } finally {
            // 回滚日志
            LoggerFactory.rollback();
        }
    }
}
