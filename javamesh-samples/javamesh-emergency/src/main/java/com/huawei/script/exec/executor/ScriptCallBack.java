/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.executor;

import com.huawei.emergency.entity.HistoryDetailEntity;
import com.huawei.script.exec.ExecResult;

/**
 * 脚本执行回调接口
 *
 * @author y30010171
 * @since 2021-10-26
 **/
public interface ScriptCallBack {
    /**
     * 执行脚本之前，回调此接口
     *
     * @param historyDetail {@link HistoryDetailEntity} 待执行的任务详情
     */
    void before(HistoryDetailEntity historyDetail);

    /**
     * 执行脚本之后，回调此接口
     *
     * @param historyDetail {@link HistoryDetailEntity} 待执行的任务详情
     * @param execResult {@link ExecResult} 执行结果
     */
    void after(HistoryDetailEntity historyDetail, ExecResult execResult);
}
