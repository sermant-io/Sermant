/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.loadbalancer.inject;

import com.huaweicloud.loadbalancer.config.LbContext;
import com.huaweicloud.loadbalancer.service.LoadbalancerConfigServiceImpl;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 负载均衡配置类
 *
 * @author zhouss
 * @since 2022-08-04
 */
@Component
public class LoadbalancerProperties {
    /**
     * 从spring获取服务名
     *
     * @param serviceName 服务名
     */
    public LoadbalancerProperties(@Value("${dubbo.application.name:${spring.application.name:application}}")
            String serviceName) {
        LbContext.INSTANCE.setServiceName(serviceName);
        final LoadbalancerConfigServiceImpl pluginService = PluginServiceManager
                .getPluginService(LoadbalancerConfigServiceImpl.class);
        pluginService.subscribe();
    }
}
