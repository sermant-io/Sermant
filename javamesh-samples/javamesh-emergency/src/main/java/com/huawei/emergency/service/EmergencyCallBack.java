/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service;

import com.huawei.emergency.entity.EmergencyExecRecord;

/**
 * 应急演练回调函数
 *
 * @author y30010171
 * @since 2021-11-04
 **/
public interface EmergencyCallBack {
    /**
     * 预案，场景，任务执行完成后的回调
     *
     * @param record {@link EmergencyExecRecord} 执行记录
     */
    void onComplete(EmergencyExecRecord record);
}
