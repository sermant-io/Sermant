/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.log;

/**
 * 脚本运行时，用于处理实时日志的回调接口
 *
 * @author y30010171
 * @since 2021-10-26
 **/
public interface LogCallBack {
    void handleLog(int id, String log);

    void handlePid(int id, String pid);
}
