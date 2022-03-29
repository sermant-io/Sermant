/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.stresstest.redis.redisson.config;

/**
 * 影子配置责任链
 *
 * @author yiwei
 * @since 2021-11-04
 */
public class ShadowConfigChains {
    private static final ShadowConfig[] CONFIGS = {new ShadowClusterConfig(), new ShadowSentinelConfig(),
        new ShadowMasterSlaveConfig(), new ShadowReplicatedConfig(), new ShadowSingleConfig()};

    private ShadowConfigChains() {
    }

    /**
     * 更新影子配置的地址信息
     *
     * @param shadowObject 影子配置
     */
    public static void update(Object shadowObject) {
        for (ShadowConfig config : CONFIGS) {
            if (config.update(shadowObject).isPresent()) {
                return;
            }
        }
    }

    /**
     * 检查当前配置是否为影子配置
     *
     * @param shadowObject 待检查配置
     * @return 影子配置返回true，否则返回false
     */
    public static boolean isShadowObject(Object shadowObject) {
        for (ShadowConfig config : CONFIGS) {
            if (config.isShadowObject(shadowObject)) {
                return true;
            }
        }
        return false;
    }
}
