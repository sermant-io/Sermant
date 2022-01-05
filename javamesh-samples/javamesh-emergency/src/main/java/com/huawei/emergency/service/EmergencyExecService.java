/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service;

import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyExecRecordDetail;
import com.huawei.emergency.entity.EmergencyPlan;
import com.huawei.emergency.entity.EmergencyScript;
import com.huawei.script.exec.log.LogResponse;

/**
 * 执行记录管理
 *
 * @author y30010171
 * @since 2021-11-13
 **/
public interface EmergencyExecService {
    CommonResult exec(EmergencyScript script);

    CommonResult debugScript(String content, String serverName);

    LogResponse getLog(int detailId, int line);

    LogResponse getRecordLog(int recordId, int line);

    CommonResult reExec(int recordId, String userName);

    CommonResult ensure(int recordId, String result, String userName);

    CommonResult stopOneServer(int detailId, String userName);

    CommonResult startOneServer(int detailId, String userName);

    CommonResult ensureOneServer(int detailId, String result, String userName);

    LogResponse logOneServer(int detailId, int line);

    CommonResult allPlanExecRecords(CommonPage<EmergencyPlan> params, String[] filterPlanNames, String[] filterCreators);

    CommonResult allSceneExecRecords(CommonPage<EmergencyExecRecord> params);

    CommonResult allTaskExecRecords(CommonPage<EmergencyExecRecord> params);
}
