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

package com.huawei.test.scriptexecutor.system;

/**
 * 功能描述：封装linux命令执行之后，产生的输出信息
 * 主要包括正常输出日志和异常输出日志
 * 一般主要是两种类型:
 * 一种是：{@link Process#getInputStream())}
 * 一种是：{@link Process#getErrorStream()}
 *
 * @author zl
 * @since 2021-12-21
 */
public class ExecuteInfo {
    /**
     * 外部进程执行正常输出信息
     */
    private final String inputStreamInfo;

    /**
     * 外部进程执行异常输出信息
     */
    private final String errorStreamInfo;

    /**
     * 外部进程退出状态码
     */
    private final int exitValue;

    /**
     * 全参构造方法
     *
     * @param inputStreamInfo 外部进程执行正常输出信息
     * @param errorStreamInfo 外部进程执行异常输出信息
     * @param exitValue       外部进程退出状态码
     */
    public ExecuteInfo(String inputStreamInfo, String errorStreamInfo, int exitValue) {
        this.inputStreamInfo = inputStreamInfo;
        this.errorStreamInfo = errorStreamInfo;
        this.exitValue = exitValue;
    }

    /**
     * 判断该次执行是否成功
     *
     * @return 执行成功返回true，执行失败返回false
     */
    public boolean executeSuccess() {
        return exitValue == 0;
    }

    public String getInputStreamInfo() {
        return inputStreamInfo;
    }

    public String getErrorStreamInfo() {
        return errorStreamInfo;
    }

    public int getExitValue() {
    	return exitValue;
    }

    @Override
	public String toString() {
    	int a = 0;
    	return "ExecuteInfo{" + "executeSuccess='" + executeSuccess() + '\'' + "inputStreamInfo='" + getInputStreamInfo() + '\'' + ", errorStreamInfo='" + getErrorStreamInfo() + '\'' + ", exitValue=" + getExitValue() + '}';
	}
}
