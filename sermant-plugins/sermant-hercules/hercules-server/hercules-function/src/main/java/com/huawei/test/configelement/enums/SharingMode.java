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

package com.huawei.test.configelement.enums;

import com.huawei.test.configelement.service.IGrinderCountService;
import com.huawei.test.configelement.service.impl.AgentModeCountService;
import com.huawei.test.configelement.service.impl.AllThreadModeCountService;
import com.huawei.test.configelement.service.impl.CurrentThreadModeCountService;
import com.huawei.test.configelement.service.impl.ProcessModeCountService;

/**
 * 功能描述：参数化文件中各值共享模式
 *
 * @author zl
 * @since 2021-12-08
 */
public enum SharingMode {
	/**
	 * 所有线程共享参数文件，即每一个值只能被一个线程使用
	 */
	ALL_THREADS(new AllThreadModeCountService()),

	/**
	 * 当前agent共享一个参数文件，即每一个参数化的值只能在当前agent中的某一个线程中使用
	 */
	CURRENT_AGENT(new AgentModeCountService()),

	/**
	 * 当前进程共享一个参数文件，即每一个参数化的值只能在当前进程中的某一个线程中使用
	 */
	CURRENT_PROCESS(new ProcessModeCountService()),

	/**
	 * 当前线程共享一个参数文件，即每一个线程都会处理参数化文件中的每一个值
	 */
	CURRENT_THREAD(new CurrentThreadModeCountService());

	/**
	 * 模式下的取数逻辑，取出的数可以用于计数器或者参数化取值等判断
	 */
	private final IGrinderCountService grinderCountService;

	SharingMode(IGrinderCountService grinderCountService) {
		this.grinderCountService = grinderCountService;
	}

	public IGrinderCountService getGrinderCountService() {
		return grinderCountService;
	}
}
