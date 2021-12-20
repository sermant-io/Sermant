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

/**
 * 功能描述：Process模式下的取数逻辑实现，即一个进程中所有线程共享一个取数逻辑，一个进程中的各个线程取数不重复
 *
 * @author zl
 * @since 2021-12-16
 */
public class ProcessModeCountService extends BaseGrinderCountService {
	@Override
	protected int doIncrement(ExecuteTimesInfo executeTimesInfo) {
		int threadNumber = executeTimesInfo.getThreadNumber();
		int runNumber = executeTimesInfo.getRunNumber();
		int threadCount = executeTimesInfo.getThreadCount();

		// 计算当前线程中已经执行到第runNumber次时，应该跨越的取值范围
		int baseByRunNumber = Math.multiplyExact(threadCount, runNumber);

		// 返回当前线程该次计数的值
		return Math.addExact(threadNumber, baseByRunNumber);
	}
}
