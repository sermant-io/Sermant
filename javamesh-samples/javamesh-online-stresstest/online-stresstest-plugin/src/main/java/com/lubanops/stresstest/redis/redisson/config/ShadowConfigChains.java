/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */
package com.lubanops.stresstest.redis.redisson.config;

/**
 * 影子配置责任链
 *
 * @author yiwei
 * @since 2021/11/4
 */
public class ShadowConfigChains {
    private static final ShadowConfig[] CONFIGS = {new ShadowClusterConfig(),
    new ShadowSentinelConfig(), new ShadowMasterSlaveConfig(), new ShadowReplicatedConfig(), new ShadowSingleConfig()};

    /**
     * 更新影子配置的地址信息
     *
     * @param shadowObject 影子配置
     */
    public static void update(Object shadowObject) {
        for (ShadowConfig config: CONFIGS) {
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
        for (ShadowConfig config: CONFIGS) {
            if (config.isShadowObject(shadowObject)) {
                return true;
            }
        }
        return false;
    }
}
