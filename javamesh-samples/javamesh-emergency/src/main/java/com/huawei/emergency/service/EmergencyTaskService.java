/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service;

import com.huawei.emergency.entity.EmergencyTask;

/**
 * 任务管理接口
 *
 * @author y30010171
 * @since 2021-11-04
 **/
public interface EmergencyTaskService extends EmergencyCommonService<EmergencyTask>, EmergencyCallBack {
    boolean isTaskExist(int taskId);
}
