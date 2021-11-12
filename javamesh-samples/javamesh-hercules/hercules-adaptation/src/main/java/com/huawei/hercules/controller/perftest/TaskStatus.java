/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.perftest;

/**
 * 功能描述：压测任务状态枚举
 *
 * @author z30009938
 * @since 2021-11-03
 */
public enum TaskStatus {
    CANCELED("CANCELED", "fail"),
    FINISHED("FINISHED", "success"),
    READY("READY", "pending"),
    SAVED("SAVED", "pending"),
    STOP_BY_ERROR("STOP_BY_ERROR", "fail"),
    TESTING("TESTING", "running");

    /**
     * 展示的状态值
     */
    private final String serverValue;

    /**
     * 服务器的状态值
     */
    private final String showValue;

    TaskStatus(String serverValue, String showValue) {
        this.showValue = showValue;
        this.serverValue = serverValue;
    }

    public String getShowValue() {
        return showValue;
    }

    public String getServerValue() {
        return serverValue;
    }

    /**
     * 根据serverValue获取前端展示value
     *
     * @param serverValue 后端保存value值
     * @return 前端展示value
     */
    public static String getShowValue(String serverValue) {
        for (TaskStatus taskStatus : values()) {
            if (taskStatus.serverValue.equals(serverValue)) {
                return taskStatus.showValue;
            }
        }
        return "";
    }

    /**
     * 根据前端展示value获取server的value
     *
     * @param showValue 前端展示value
     * @return server的value
     */
    public static String getServerValue(String showValue) {
        for (TaskStatus taskInfoKey : values()) {
            if (taskInfoKey.showValue.equals(showValue)) {
                return taskInfoKey.serverValue;
            }
        }
        return "";
    }
}