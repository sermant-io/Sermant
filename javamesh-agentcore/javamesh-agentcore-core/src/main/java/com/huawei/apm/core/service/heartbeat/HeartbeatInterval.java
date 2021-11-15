/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.service.heartbeat;

/**
 * 心跳间隔建议枚举
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public enum HeartbeatInterval {
    /**
     * 每1帧{@link #HEARTBEAT_INTERVAL}都会发送心跳
     */
    ALWAYS(1),
    /**
     * 每2帧{@link #HEARTBEAT_INTERVAL}发送一次心跳
     */
    USUALLY(2),
    /**
     * 每3帧{@link #HEARTBEAT_INTERVAL}发送一次心跳
     */
    OFTEN(3),
    /**
     * 每5帧{@link #HEARTBEAT_INTERVAL}发送一次心跳
     */
    SOMETIMES(5),
    /**
     * 每10帧{@link #HEARTBEAT_INTERVAL}发送一次心跳
     */
    SELDOM(10),
    /**
     * 每30帧{@link #HEARTBEAT_INTERVAL}发送一次心跳
     */
    RARELY(30),
    /**
     * 每60帧{@link #HEARTBEAT_INTERVAL}发送一次心跳
     */
    HARDLY_EVER(60);

    /**
     * 每一帧心跳的间隔，现为1s
     */
    private static final long HEARTBEAT_INTERVAL = 1000;

    /**
     * 帧数
     */
    private final int frames;

    HeartbeatInterval(int frames) {
        this.frames = frames;
    }

    public int getFrames() {
        return frames;
    }

    /**
     * 休眠1帧
     */
    public static void sleepMinimalInterval() {
        try {
            Thread.sleep(HEARTBEAT_INTERVAL);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * 计算间隔时间，为帧数 * 一帧间隔时间{@link #HEARTBEAT_INTERVAL}
     *
     * @param frames 帧数
     * @return 间隔时间
     */
    public static long getInterval(int frames) {
        return frames * HEARTBEAT_INTERVAL;
    }
}
