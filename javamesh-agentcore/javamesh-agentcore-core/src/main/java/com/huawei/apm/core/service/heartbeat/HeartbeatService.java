/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.service.heartbeat;

import java.util.Map;

import com.huawei.apm.core.service.CoreService;

/**
 * 心跳服务
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public interface HeartbeatService extends CoreService {
    /**
     * 注册心跳
     *
     * @param heartbeatName 心跳名称，用于区分不同类型心跳的名字，不可重复
     */
    void heartbeat(String heartbeatName);

    /**
     * 注册心跳
     *
     * @param heartbeatName 心跳名称，用于区分不同类型心跳的名字，不可重复
     * @param infoMap       其他信息集合
     */
    void heartbeat(String heartbeatName, Map<String, String> infoMap);

    /**
     * 注册心跳
     *
     * @param heartbeatName   心跳名称，用于区分不同类型心跳的名字，不可重复
     * @param infoMapProvider 其他信息的提供者
     * @param interval        心跳间隔
     */
    void heartbeat(String heartbeatName, InfoMapProvider infoMapProvider, HeartbeatInterval interval);

    /**
     * 注册心跳
     *
     * @param heartbeatName   心跳名称，用于区分不同类型心跳的名字，不可重复
     * @param infoMapProvider 其他信息的提供者
     * @param frames          帧数，意义参考{@link HeartbeatInterval#getFrames()}
     */
    void heartbeat(String heartbeatName, InfoMapProvider infoMapProvider, int frames);

    /**
     * 停止心跳发送
     *
     * @param heartbeatName 心跳名称
     */
    void stopHeartbeat(String heartbeatName);

    /**
     * 信息集合提供者，当心跳发送的内容会发生改变是，需要定制信息提供方式
     */
    interface InfoMapProvider {
        /**
         * 提供信息集合
         *
         * @return 信息集合
         */
        Map<String, String> provide();
    }
}
