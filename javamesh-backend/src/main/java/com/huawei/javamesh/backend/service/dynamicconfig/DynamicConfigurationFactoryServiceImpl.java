/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.backend.service.dynamicconfig;

import com.huawei.javamesh.backend.service.dynamicconfig.kie.KieDynamicConfigurationServiceImpl;
import com.huawei.javamesh.backend.service.dynamicconfig.nop.NopDynamicConfigurationService;
import com.huawei.javamesh.backend.service.dynamicconfig.service.DynamicConfigType;
import com.huawei.javamesh.backend.service.dynamicconfig.service.DynamicConfigurationFactoryService;
import com.huawei.javamesh.backend.service.dynamicconfig.service.DynamicConfigurationService;
import com.huawei.javamesh.backend.service.dynamicconfig.zookeeper.ZookeeperDynamicConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 *
 * The implementation for the DynamicConfigurationFactoryService
 *
 */
@Component
public class DynamicConfigurationFactoryServiceImpl implements DynamicConfigurationFactoryService {

    private static final Logger logger = LoggerFactory.getLogger(DynamicConfigurationFactoryServiceImpl.class);

    protected DynamicConfigurationService getDynamicConfigurationService(DynamicConfigType dct) {

        if ( dct == DynamicConfigType.ZOO_KEEPER )
            return ZookeeperDynamicConfigurationService.getInstance();

        if ( dct == DynamicConfigType.KIE )
            return KieDynamicConfigurationServiceImpl.getInstance();

        if ( dct == DynamicConfigType.NOP )
            return NopDynamicConfigurationService.getInstance();

        return null;
    }

    @Override
    public DynamicConfigurationService getDynamicConfigurationService() {
        return this.getDynamicConfigurationService(Config.getDynamic_config_type());
    }
}
