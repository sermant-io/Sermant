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

package com.huawei.test.configelement.service.impl;

import com.huawei.test.configelement.service.BaseGrinderCountService;
import com.huawei.test.configelement.service.ExecuteTimesInfo;
import com.huawei.test.exception.FunctionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.*;

/**
 * 功能描述：All thread模式下的取数逻辑实现，整个压测任务的所有线程共享一个取数逻辑，每一个线程取到的数据都不会出现重复
 *
 * @author zl
 * @since 2021-12-16
 */
public class AllThreadModeCountService extends BaseGrinderCountService {
	@Override
	protected int doIncrement(ExecuteTimesInfo executeTimesInfo) {
		int agentNumber = executeTimesInfo.getAgentNumber();
		int processNumber = executeTimesInfo.getProcessNumber();
		int threadNumber = executeTimesInfo.getThreadNumber();
		int runNumber = executeTimesInfo.getRunNumber();
		int agentCount = executeTimesInfo.getAgentCount();
		int processCount = executeTimesInfo.getProcessCount();
		int threadCount = executeTimesInfo.getThreadCount();

		// 计算当前线程所在agent的序号之前的agent占用的取数范围
		int baseByAgentNumber = multiplyExact(multiplyExact(agentNumber, processCount), threadCount);

		// 计算当前线程所在的进程的序号之前的进程占用的取数范围
		int baseByProcessNumber = multiplyExact(processNumber, threadCount);

		// 计算当前线程中已经执行到第runNumber次时，应该跨越的取值范围
		int baseByRunNumber = multiplyExact(multiplyExact(multiplyExact(agentCount, processCount), threadCount), runNumber);

		// 返回当前线程该次计数的值
		return addExact(addExact(addExact(baseByAgentNumber, baseByProcessNumber), threadNumber), baseByRunNumber);
	}
}
