/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.implement.operation.initializer;

import com.huaweicloud.sermant.core.operation.initializer.DynamicConfigServiceInitializer;
import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.KieDynamicConfigService;
import com.huaweicloud.sermant.implement.service.dynamicconfig.zookeeper.ZooKeeperDynamicConfigService;

/**
 * Dynamic configuration service initialization
 *
 * @author luanwenfei
 * @since 2022-06-29
 */
public class DynamicConfigServiceInitializerImpl implements DynamicConfigServiceInitializer {
    @Override
    public DynamicConfigService initKieDynamicConfigService(String serverAddress, String project) {
        if (StringUtils.isBlank(serverAddress) || StringUtils.isBlank(project)) {
            return new KieDynamicConfigService();
        }
        return new KieDynamicConfigService(serverAddress, project);
    }

    @Override
    public DynamicConfigService initZookeeperConfigService() {
        return new ZooKeeperDynamicConfigService();
    }
}
