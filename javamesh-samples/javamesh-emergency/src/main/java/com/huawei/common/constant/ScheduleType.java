/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.common.constant;

import lombok.Getter;

/**
 * @author y30010171
 * @since 2021-11-19
 **/
@Getter
public enum ScheduleType {
    NONE("0", "NONE"), // 立即运行一次
    ONCE("1", "ONCE"), // 运行一次
    CORN("2", "CORN"), // corn表达式
    FIX_DATE("3", "FIX_DATE"); // 固定间隔


    private String value;
    private String description;

    ScheduleType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static ScheduleType match(String value, ScheduleType defaultItem) {
        for (ScheduleType item : ScheduleType.values()) {
            if (value.equals(item.value)) {
                return item;
            }
        }
        return defaultItem;
    }
}
