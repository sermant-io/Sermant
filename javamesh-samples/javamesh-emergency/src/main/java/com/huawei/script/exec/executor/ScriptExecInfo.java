/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.executor;

import com.huawei.script.exec.session.ServerInfo;

import lombok.Data;

/**
 * 待执行的脚本信息
 *
 * @author y30010171
 * @since 2021-10-20
 **/
@Data
public class ScriptExecInfo {
    /**
     * 任务详情ID
     */
    private int id;
    /**
     * 脚本名称
     */
    private String scriptName;
    /**
     * 脚本内容
     */
    private String scriptContext;
    /**
     * 脚本的存放路径
     */
    private String scriptLocation;
    /**
     * <p>运行此脚本所需要的远程服务器信息</p>
     * <p>如果为本地执行，则无需理会此字段 </p>
     */
    private ServerInfo remoteServerInfo;

    /**
     * 运行参数
     */
    private String[] params;

    /**
     * 超时时间
     */
    private long timeOut;
}
