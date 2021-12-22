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

package com.huawei.test.configelement;

import com.huawei.test.PressureTestFunction;
import com.huawei.test.configelement.service.ExecuteTimesInfo;
import net.grinder.script.Grinder;

/**
 * 功能描述：需要通过配置初始化的函数
 *
 * @author zl
 * @since 2021-12-09
 */
public abstract class ConfigElement<T> implements PressureTestFunction {
	/**
	 * 函数名称
	 */
	private String functionName;

	/**
	 * 函数注释
	 */
	private String functionComments;

	/**
	 * 初始化函数配置
	 *
	 * @param config 配置信息
	 */
	public abstract void initConfig(T config);

	@Override
	public void defineFunctionName(String name) {
		this.functionName = name;
	}

	@Override
	public void addComments(String comments) {
		this.functionComments = comments;
	}

	/**
	 * 获取每一个线程执行过程中的agent，process，thread，runNumber等信息
	 *
	 * @return 个线程执行过程中的agent，process，thread，runNumber等信息封装的bean实例
	 */
	public ExecuteTimesInfo getExecuteTimesInfo() {
		int totalAgents = Integer.parseInt(Grinder.grinder.getProperties().get("grinder.agents").toString()); // 设置的总加压机数
		int totalProcesses = Integer.parseInt(Grinder.grinder.getProperties().get("grinder.processes").toString()); // 设置的总进程数
		int totalThreads = Integer.parseInt(Grinder.grinder.getProperties().get("grinder.threads").toString()); // 设置的总线程数
		int agentNumber = Grinder.grinder.getAgentNumber(); // 当前运行的加压机编号
		int processNumber = Grinder.grinder.getProcessNumber(); // 当前运行的进程编号
		int threadNumber = Grinder.grinder.getThreadNumber(); // 当前运行的线程编号
		int runNumber = Grinder.grinder.getRunNumber(); // 当前运行的运行次数编号
		return new ExecuteTimesInfo.Builder()
			.setAgentCount(totalAgents)
			.setProcessCount(totalProcesses)
			.setThreadCount(totalThreads)
			.setAgentNumber(agentNumber)
			.setProcessNumber(processNumber)
			.setThreadNumber(threadNumber)
			.setRunNumber(runNumber)
			.build();
	}

	/**
	 * 判断配置是否合法
	 *
	 * @return true:合法， false：不合法
	 */
	public abstract boolean isConfigValid();
}
