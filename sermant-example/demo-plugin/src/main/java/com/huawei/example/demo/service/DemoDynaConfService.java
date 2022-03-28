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

package com.huawei.example.demo.service;

import com.huawei.example.demo.common.DemoLogger;
import com.huawei.sermant.core.plugin.service.PluginService;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

/**
 * 动态配置示例
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-26
 */
public class DemoDynaConfService implements PluginService {
    private DynamicConfigService service;

    /**
     * 如果是zookeeper实现，修改{@code /sermant/demo/test}的值以观察动态配置效果
     */
    @Override
    public void start() {
        service = ServiceManager.getService(DynamicConfigService.class);
        service.addConfigListener("demo/test", "sermant", new DynamicConfigListener() {
            @Override
            public void process(DynamicConfigEvent event) {
                DemoLogger.println("[DemoDynaConfService]-" + event.toString());
            }
        });
    }

    @Override
    public void stop() {
        service.stop();
    }
}
