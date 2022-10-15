/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.service.lb;

import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.LbService;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 负载均衡服务
 *
 * @author zhouss
 * @since 2022-09-26
 */
public class LbServiceImpl implements LbService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public Optional<ServiceInstance> choose(String serviceName) {
        return DiscoveryManager.INSTANCE.choose(serviceName);
    }

    @Override
    public void stop() {
        try {
            DiscoveryManager.INSTANCE.stop();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Stop lb service failed!", ex);
        }
    }
}
