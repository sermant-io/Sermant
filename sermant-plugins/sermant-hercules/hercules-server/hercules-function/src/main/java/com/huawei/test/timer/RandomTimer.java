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

package com.huawei.test.timer;

import com.huawei.test.exception.FunctionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 功能描述：随机线程时间停止
 *
 * @author zl
 * @since 2021-12-09
 */
public class RandomTimer {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(RandomTimer.class);

	/**
	 * 随机停止指定单位的时间长度，但是取值是从start-end之间
	 *
	 * @param start    停止时间计算时开始时间
	 * @param end      停止时间计算时结束时间
	 * @param timeUnit 时间单位
	 */
	public static void delay(long start, long end, TimeUnit timeUnit) {
		ConstantTimer.delay(randomTimestamp(start, end), timeUnit);
	}

	/**
	 * start和end之间的随机timestamp
	 *
	 * @param start 开始timestamp
	 * @param end   结束timestamp
	 * @return start和end之间的随机timestamp
	 */
	public static long randomTimestamp(long start, long end) {
		if (start <= 0 || end <= 0) {
			throw new FunctionException("End time and start time must great than 0.");
		}
		if (end <= start) {
			throw new FunctionException("End time must great than start time.");
		}
		double randomValue = Math.random();
		long timeZone = end - start;
		long randomTimeValue = (long) (timeZone * randomValue);
		return start + randomTimeValue;
	}
}
