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

package com.huaweicloud.sermant.core.operation.initializer;

import com.huaweicloud.sermant.core.operation.BaseOperation;
import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;

/**
 * 动态配置服务初始化器
 *
 * @author luanwenfei
 * @since 2022-06-29
 */
public interface DynamicConfigServiceInitializer extends BaseOperation {
    /**
     * Init kie dynamic config service.
     *
     * @param serverAddress serverAddress
     * @param project namespace
     * @return A KieDynamicConfigService instance
     */
    DynamicConfigService initKieDynamicConfigService(String serverAddress, String project);

    /**
     * Init zookeeper dynamic config service.
     *
     * @return A ZooKeeperDynamicConfigService instance.
     */
    DynamicConfigService initZookeeperConfigService();
}
