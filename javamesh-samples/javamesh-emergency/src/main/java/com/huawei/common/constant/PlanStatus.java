/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.common.constant;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author y30010171
 * @since 2021-11-26
 **/
@Getter
public enum PlanStatus {


    NEW("0", "unapproved", "新增"),
    APPROVING("1", "approving", "待审核"),
    APPROVED("2", "approved", "已审核"),
    REJECT("3", "unapproved", "拒绝"),
    RUNNING("4", "running", "运行中"),
    SUCCESS("5", "ran", "运行成功"),
    FAILED("6", "ran", "运行失败");

    public static final List<String> UN_PASSED_STATUS = Arrays.asList(
        PlanStatus.NEW.getValue(),
        PlanStatus.APPROVING.getValue(),
        PlanStatus.REJECT.getValue()
    );

    private String value;
    private String status;
    private String statusLabel;

    PlanStatus(String value, String status, String statusLabel) {
        this.value = value;
        this.status = status;
        this.statusLabel = statusLabel;
    }

    public static PlanStatus matchByLabel(String statusLabel, PlanStatus defaultStatus) {
        for (PlanStatus status : PlanStatus.values()) {
            if (status.getStatusLabel().equals(statusLabel)) {
                return status;
            }
        }
        return defaultStatus;
    }
}
