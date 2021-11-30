/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.domain;

/**
 * 回放过程中上报zookeeper的各种状态
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-03-15
 */
public enum FlowReplayStatus {
    /**
     * FlowReplayWorker 等待获取锁
     */
    WAIT,
    /**
     * FlowReplayWorker 闲置
     */
    IDLE,
    /**
     * FlowReplayWorker 占用
     */
    BUSY,
    /**
     * FlowReplayTasks 处理中
     */
    RUNNING,
    /**
     * FlowReplayTasks 完成
     */
    DONE,
    /**
     * FlowReplayTasks 终止
     */
    STOPPED
}
