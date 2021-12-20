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

package com.huawei.test.configelement.impl;

import com.huawei.test.configelement.Counter;
import com.huawei.test.configelement.config.CounterConfig;
import com.huawei.test.configelement.service.ExecuteTimesInfo;
import com.huawei.test.exception.FunctionException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * 功能描述：计数器实现
 *
 * @author zl
 * @since 2021-12-09
 */
public class CommonCounter extends Counter {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonCounter.class);

	/**
	 * config配置
	 */
	private CounterConfig counterConfig;

	@Override
	public void initConfig(CounterConfig config) {
		this.counterConfig = config;
	}

	@Override
	public boolean isConfigValid() {
		if (counterConfig == null) {
			LOGGER.error("The counter config is null.");
			return false;
		}
		return counterConfig.getSharingMode() != null;
	}

	@Override
	public String nextNumber() {
		if (!isConfigValid()) {
			throw new FunctionException("The counter config is invalid.");
		}
		int nextValue;
		try {
			nextValue = nextValue();
		} catch (ArithmeticException arithmeticException) {
			LOGGER.error("Get next value fail when counter, reason:{}", arithmeticException.getMessage());
			throw new FunctionException("Get next value fail when counter");
		}
		Integer maxValue = counterConfig.getMaxValue();
		if(maxValue != null && nextValue > maxValue) {
			LOGGER.error("Over max value:{}", counterConfig.getMaxValue());
			throw new FunctionException("Over max value.");
		}
		String numberFormat = counterConfig.getNumberFormat();
		if (StringUtils.isEmpty(numberFormat)) {
			return String.valueOf(nextValue);
		}
		return String.format(Locale.ENGLISH, numberFormat, nextValue);
	}

	@Override
	public boolean hasNext() {
		if (!isConfigValid()) {
			return false;
		}
		Integer maxValue = counterConfig.getMaxValue();
		int nextValue;
		try {
			nextValue = nextValue();
		} catch (ArithmeticException arithmeticException) {
			LOGGER.error("Get next value fail when counter, reason:{}", arithmeticException.getMessage());
			return false;
		}
		return maxValue == null || nextValue <= maxValue;
	}

	/**
	 * 通过config配置计算下一个值
	 *
	 * @return 下一个值
	 */
	protected int nextValue() {
		ExecuteTimesInfo executeTimesInfo = getExecuteTimesInfo();
		int nextValue = counterConfig.getSharingMode().getGrinderCountService().nextIncrementNumber(executeTimesInfo);
		int startValue = counterConfig.getStartValue();
		int incrementRange = counterConfig.getIncrement();
		return Math.addExact(Math.multiplyExact(nextValue, incrementRange), startValue);
	}
}
