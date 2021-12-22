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

package com.huawei.test.configelement.service;

import com.huawei.test.exception.FunctionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 功能描述：Grinder底层服务调用
 *
 * @author zl
 * @since 2021-12-15
 */
public abstract class BaseGrinderCountService implements IGrinderCountService {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseGrinderCountService.class);

	@Override
	public int nextIncrementNumber(ExecuteTimesInfo executeTimesInfo) {
		if (!isValid(executeTimesInfo)) {
			LOGGER.error("Data for counter:{}", executeTimesInfo);
			throw new FunctionException("The execute info is invalid.");
		}
		LOGGER.debug("Data for counter:{}", executeTimesInfo);
		return doIncrement(executeTimesInfo);
	}

	/**
	 * 实现各自的自增逻辑
	 *
	 * @param executeTimesInfo 当前线程执行消息
	 * @return 下一个取数
	 */
	protected abstract int doIncrement(ExecuteTimesInfo executeTimesInfo);

	/**
	 * 判断该线程执行逻辑时的参数是否支持取数
	 *
	 * @param executeTimesInfo 传入的线程执行信息
	 * @return true：合法，false：不合法
	 */
	protected boolean isValid(ExecuteTimesInfo executeTimesInfo) {
		if (executeTimesInfo == null) {
			return false;
		}
		return executeTimesInfo.getThreadCount() > 0
			&& executeTimesInfo.getAgentCount() > 0
			&& executeTimesInfo.getProcessCount() > 0
			&& executeTimesInfo.getAgentNumber() >= 0
			&& executeTimesInfo.getProcessNumber() >= 0
			&& executeTimesInfo.getThreadNumber() >= 0
			&& executeTimesInfo.getRunNumber() >= 0;
	}
}
