/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.log;

/**
 * 记录每次脚本执行的实时日志
 *
 * @author y30010171
 * @since 2021-10-26
 **/
public class DefaultLogCallBack implements LogCallBack {
    /**
     * 任务id
     */
    private int id;

    public DefaultLogCallBack(int id) {
        this.id = id;
    }

    @Override
    public void handle(String log) {
        LogMemoryStore.addLog(id, new String[]{log});
    }
}
