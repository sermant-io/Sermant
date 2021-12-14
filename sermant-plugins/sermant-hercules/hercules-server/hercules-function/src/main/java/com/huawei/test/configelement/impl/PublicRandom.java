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

import java.util.Date;

/**
 * 功能描述：随机函数实现
 *
 * @author zl
 * @since 2021-12-09
 */
public class PublicRandom {
	/**
	 * 1970-1-1开始的随机时间字符串，例如19700101000000，表示1970-01-01 00:00:00
	 *
	 * @param dateFormat 显示时间格式，默认是yyyyMMddHHmmss
	 * @return 时间格式字符串
	 */
	public static String randomTime(String dateFormat) {
		return null;
	}

	/**
	 * 返回一个从开始时间start到截止时间end之间的随机时间字符串
	 *
	 * @param start 开始时间
	 * @param end 截止时间
	 * @param dateFormat 时间字符串格式
	 * @return 指定格式的时间字符串
	 */
	public static String randomTime(Date start, Date end, String dateFormat) {
		return null;
	}

	/**
	 * 返回指定长度的随机字符串，字符集中所有字符都有可能
	 *
	 * @param length 需要的字符串长度
	 * @return 长度为length的字符串
	 */
	public static String randomString(int length) {
		return null;
	}

	/**
	 * 返回指定字符数组中的长度随机字符串，字符数组中所有字符都有可能
	 *
	 * @param length 需要的字符串长度
	 * @return 长度为length的字符串
	 */
	public static String randomString(char[] chars, int length) {
		return null;
	}

	/**
	 * 返回纯字母大小写随机的指定长度字符串
	 *
	 * @param length 字符串需求的长度
	 * @return 长度为length的随机纯字母字符串
	 */
	public static String randomAlphabetic(int length) {
		return null;
	}

	/**
	 * 返回纯字母和数字组成的大小写随机的指定长度字符串
	 *
	 * @param length 字符串需求的长度
	 * @return 长度为length的随机纯字母和数字组成的字符串
	 */
	public static String randomAlphaNumeric(int length) {
		return null;
	}

	/**
	 * 返回数字组成的随机的指定长度字符串
	 *
	 * @param length 字符串需求的长度
	 * @return 长度为length的数字组成的字符串
	 */
	public static String randomNumeric(int length) {
		return null;
	}

	/**
	 * 返回ascii组成的随机的指定长度字符串
	 *
	 * @param length 字符串需求的长度
	 * @return 长度为length的ascii组成的字符串
	 */
	public static String randomAscii(int length) {
		return null;
	}

	/**
	 * 返回随机整数
	 *
	 * @return 随机整数
	 */
	public static int randomInt() {
		return 0;
	}

	/**
	 * 返回start和end之间的随机整数
	 *
	 * @param start 开始的整数值
	 * @param end 结束的整数值
	 * @return start和end之间的随机整数值
	 */
	public static int randomInt(int start, int end) {
		return 0;
	}

	/**
	 * 返回随机长整型数字
	 *
	 * @return 随机长整型数字
	 */
	public static long randomLong() {
		return 0;
	}

	/**
	 * 返回start和end之间的随机长整型数字
	 *
	 * @param start 开始值
	 * @param end 结束值
	 * @return 返回start和end之间的随机长整型数字
	 */
	public static long randomLong(long start, long end) {
		return 0;
	}

	/**
	 * 返回随机浮点数
	 *
	 * @param scale 精度
	 * @return 随机浮点数
	 */
	public static float randomFloat(int scale) {
		return 0;
	}

	/**
	 * 返回start和end之间的随机浮点数
	 *
	 * @param start 开始值
	 * @param end 结束值
	 * @param scale 精度
	 * @return start和end之间的随机浮点数
	 */
	public static float randomFloat(float start, float end, int scale) {
		return 0;
	}
}
