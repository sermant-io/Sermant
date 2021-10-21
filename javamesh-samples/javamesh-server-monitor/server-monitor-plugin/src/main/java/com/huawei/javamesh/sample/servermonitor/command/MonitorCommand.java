/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.command;

import java.io.InputStream;

/**
 * 一个{@link MonitorCommand}包含了获取Linux指令、指令执行结果解析和错误处理的方法，
 * 参考{@link CommandExecutor}
 *
 * @param <T> 执行指令对应的外部进程输出流的解析结果类型
 */
public interface MonitorCommand<T> {

    /**
     * 返回Linux指令
     *
     * @return linux指令
     */
    String getCommand();

    /**
     * 结果解析，解析外部进程的输出流
     *
     * @param inputStream 外部进程输出流
     * @return 解析后的结果
     */
    T parseResult(InputStream inputStream);

    /**
     * 错误处理，处理外部进程的错误流
     *
     * @param errorStream 外部进程错误流
     */
    void handleError(InputStream errorStream);
}
