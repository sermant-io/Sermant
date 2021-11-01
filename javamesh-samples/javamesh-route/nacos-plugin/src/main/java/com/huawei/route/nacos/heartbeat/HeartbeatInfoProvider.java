/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.nacos.heartbeat;

import com.huawei.apm.bootstrap.boot.heartbeat.HeartbeatService;

import java.util.HashMap;
import java.util.Map;

/**
 * 心跳数据生产
 *
 * @author zhouss
 * @since 2021-11-01
 */
public class HeartbeatInfoProvider implements HeartbeatService.InfoMapProvider {
    private static final HeartbeatInfoProvider INSTANCE = new HeartbeatInfoProvider();

    private final Map<String, String> infoMap = new HashMap<String, String>(4);

    @Override
    public Map<String, String> provide() {
        return infoMap;
    }

    /**
     * 获取心跳提供者单例
     *
     * @return HeartbeatInfoProvider
     */
    public static HeartbeatInfoProvider getInstance() {
        return INSTANCE;
    }

    /**
     * 注册心跳数据
     *
     * @param key 键
     * @param value 值
     * @return HeartbeatInfoProvider
     */
    public HeartbeatInfoProvider registerHeartMsg(String key, String value) {
        infoMap.put(key, value);
        return this;
    }
}
