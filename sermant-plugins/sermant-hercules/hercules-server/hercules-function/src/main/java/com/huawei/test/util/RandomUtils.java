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

package com.huawei.test.util;

import com.huawei.test.exception.FunctionException;
import com.huawei.test.timer.RandomTimer;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 功能描述：随机函数实现
 *
 * @author zl
 * @since 2021-12-09
 */
public class RandomUtils {
	/**
	 * 返回一个从开始时间start到截止时间end之间的随机时间字符串
	 *
	 * @param start      开始时间
	 * @param end        截止时间
	 * @param dateFormat 时间字符串格式
	 * @return 指定格式的时间字符串
	 */
	public static String randomTime(Date start, Date end, String dateFormat) {
		String realDateFormat = "yyyy-MM-dd HH:mm:ss";
		if (!StringUtils.isEmpty(dateFormat)) {
			realDateFormat = dateFormat;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(realDateFormat, Locale.ENGLISH);
		simpleDateFormat.setTimeZone(TimeZone.getDefault());
		if (start == null || end == null) {
			throw new FunctionException("Start or end date is null.");
		}
		long startTimestamp = start.getTime();
		long endTimestamp = end.getTime();
		long randomTimestamp = RandomTimer.randomTimestamp(startTimestamp, endTimestamp);
		return simpleDateFormat.format(new Date(randomTimestamp));
	}

	/**
	 * 返回指定长度的随机字符串，字符集中所有字符都有可能
	 *
	 * @param length 需要的字符串长度
	 * @return 长度为length的字符串
	 */
	public static String randomString(int length) {
		if (length <= 0) {
			return "";
		}
		return RandomStringUtils.random(length);
	}

	/**
	 * 返回指定字符数组中的长度随机字符串，字符数组中所有字符都有可能
	 *
	 * @param chars  字符集
	 * @param length 需要的字符串长度
	 * @return 长度为length的字符串
	 */
	public static String randomString(char[] chars, int length) {
		if (length <= 0 || chars == null || chars.length == 0) {
			return "";
		}
		return RandomStringUtils.random(length, chars);
	}

	/**
	 * 返回指定字符数组中的长度随机字符串，字符数组中所有字符都有可能
	 *
	 * @param chars  字符集字符串
	 * @param length 需要的字符串长度
	 * @return 长度为length的字符串
	 */
	public static String randomString(String chars, int length) {
		if (length <= 0 || chars == null || chars.length() == 0) {
			return "";
		}
		return RandomStringUtils.random(length, chars);
	}

	/**
	 * 返回纯字母大小写随机的指定长度字符串
	 *
	 * @param length 字符串需求的长度
	 * @return 长度为length的随机纯字母字符串
	 */
	public static String randomAlphabetic(int length) {
		if (length <= 0) {
			return "";
		}
		return RandomStringUtils.randomAlphabetic(length);
	}

	/**
	 * 返回纯字母和数字组成的大小写随机的指定长度字符串
	 *
	 * @param length 字符串需求的长度
	 * @return 长度为length的随机纯字母和数字组成的字符串
	 */
	public static String randomAlphaNumeric(int length) {
		if (length <= 0) {
			return "";
		}
		return RandomStringUtils.randomAlphanumeric(length);
	}

	/**
	 * 返回数字组成的随机的指定长度字符串
	 *
	 * @param length 字符串需求的长度
	 * @return 长度为length的数字组成的字符串
	 */
	public static String randomNumeric(int length) {
		if (length <= 0) {
			return "";
		}
		return RandomStringUtils.randomNumeric(length);
	}

	/**
	 * 返回ascii组成的随机的指定长度字符串
	 *
	 * @param length 字符串需求的长度
	 * @return 长度为length的ascii组成的字符串
	 */
	public static String randomAscii(int length) {
		if (length <= 0) {
			return "";
		}
		return RandomStringUtils.randomAscii(length);
	}

	/**
	 * 返回随机整数
	 *
	 * @return 随机整数
	 */
	public static int randomInt() {
		return org.apache.commons.lang.math.RandomUtils.nextInt();
	}

	/**
	 * 返回start和end之间的随机整数
	 *
	 * @param start 开始的整数值
	 * @param end   结束的整数值
	 * @return start和end之间的随机整数值
	 */
	public static int randomInt(int start, int end) {
		if (end <= start) {
			throw new FunctionException("The end value must great than start value.");
		}
		return start + org.apache.commons.lang.math.RandomUtils.nextInt(end - start);
	}
}
