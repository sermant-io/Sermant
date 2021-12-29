/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.plugin.servermonitor.command;

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
