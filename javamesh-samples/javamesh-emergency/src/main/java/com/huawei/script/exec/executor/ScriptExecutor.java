/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.executor;

import com.huawei.script.exec.ExecResult;
import com.huawei.script.exec.log.LogCallBack;
import com.huawei.script.exec.session.ServerInfo;

/**
 * 脚本执行器
 *
 * @author y30010171
 * @since 2021-10-26
 **/
public interface ScriptExecutor {
    /**
     * 本地执行模式
     */
    String LOCAL = "0";
    /**
     * 远程执行模式
     */
    String REMOTE = "1";

    /**
     * 获取执行器的执行模式
     *
     * @return 执行模式
     */
    String mode();

    /**
     * 远程执行脚本
     *
     * <p>1. 创建脚本文件</p>
     * <p>2. 执行脚本文件</p>
     * <p>3. 删除脚本文件</p>
     *
     * @param scriptExecInfo {@link ScriptExecInfo} 待执行的脚本信息
     * @param logCallback    {@link LogCallBack} 日志回调接口，处理实时产生的日志
     * @return {@code ExecResult} <p>通过{@link ExecResult#isSuccess()} 判断是否执行成功</p>
     * <p>通过{@link ExecResult#getMsg()}获取执行时的所有日志信息</p>
     */
    ExecResult execScript(ScriptExecInfo scriptExecInfo, LogCallBack logCallback);

    ExecResult cancel(ServerInfo serverInfo,int pid);
}
