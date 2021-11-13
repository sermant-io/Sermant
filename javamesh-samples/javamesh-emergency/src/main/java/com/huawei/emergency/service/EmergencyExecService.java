/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service;

import com.huawei.common.api.CommonResult;
import com.huawei.emergency.entity.EmergencyScript;
import com.huawei.script.exec.log.LogRespone;

/**
 * 执行记录管理
 *
 * @author y30010171
 * @since 2021-11-13
 **/
public interface EmergencyExecService {
    CommonResult exec(EmergencyScript script);
    LogRespone getLog(int recordId,int line);
}
