/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.init;

import com.huawei.flowcontrol.common.adapte.cse.rule.syncer.CseRuleSyncer;
import com.huawei.flowcontrol.common.adapte.cse.rule.syncer.DefaultRuleSyncer;
import com.huawei.flowcontrol.common.adapte.cse.rule.syncer.RuleSyncer;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.factory.FlowControlThreadFactory;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.plugin.service.PluginService;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 流控插件 公共能力 统一初始化入口
 *
 * @author zhouss
 * @since 2022-01-25
 */
public class FlowControlInitServiceImpl implements PluginService {
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0,
        TimeUnit.SECONDS, new SynchronousQueue<>(), new FlowControlThreadFactory("FLOW_CONTROL_INIT_THREAD"));

    private final FlowControlLifeCycle flowControlLifeCycle = new FlowControlLifeCycle();

    @Override
    public void start() {
        executor.execute(flowControlLifeCycle);
    }

    @Override
    public void stop() {
        flowControlLifeCycle.stop();
    }

    static class FlowControlLifeCycle implements Runnable {
        private RuleSyncer ruleSyncer;

        @Override
        public void run() {
            final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
            if (pluginConfig.isUseCseRule()) {
                // 适配cse, 开始适配cse的专用配置监听器
                ruleSyncer = new CseRuleSyncer();
            } else {
                ruleSyncer = new DefaultRuleSyncer();
            }
            ruleSyncer.start();
        }

        public void stop() {
            if (ruleSyncer != null) {
                ruleSyncer.stop();
            }
        }
    }
}
