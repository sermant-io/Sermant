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

import com.huawei.test.configelement.service.IParamFileLineSplitter;
import com.huawei.test.exception.FunctionException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.huawei.test.configelement.service.impl.CharType.BACKSLASH;
import static com.huawei.test.configelement.service.impl.CharType.COMMON;
import static com.huawei.test.configelement.service.impl.CharType.DELIMITER;
import static com.huawei.test.configelement.service.impl.CharType.LINE_BREAK;
import static com.huawei.test.configelement.service.impl.CharType.QUOT;


/**
 * 功能描述：参数化文件中，每一行中的某一列数据可以使用双引号把值包围起来，能包括分隔符在里面，避免直接按照分隔符分割数据时，造成数据破坏
 *
 * @author zl
 * @since 2021-12-17
 */
public class QuotedDataParamFileLineSplitter implements IParamFileLineSplitter {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(QuotedDataParamFileLineSplitter.class);

	@Override
	public List<String> splitLine(String lineContent, String delimiter) {
		if (StringUtils.isEmpty(lineContent)) {
			return Collections.emptyList();
		}
		if (StringUtils.isEmpty(delimiter)) {
			List<String> values = new ArrayList<>(1);
			values.add(lineContent);
			return values;
		}
		char[] chars = delimiter.toCharArray();
		if (chars.length > 1) {
			throw new FunctionException("Delimiter should only one char");
		}
		return doSplit(lineContent, chars[0]);
	}

	/**
	 * 实际把一行内容根据分割符和引号的使用，分割成需要的字段
	 *
	 * @param lineContent 一行内容
	 * @param delimiter   分隔符
	 * @return 一行内容的列表
	 */
	public List<String> doSplit(String lineContent, char delimiter) {
		if (StringUtils.isEmpty(lineContent)) {
			return Collections.emptyList();
		}
		char[] chars = lineContent.toCharArray();

		// 初始化扫描开始状态
		Status previousStatus = Status.START;

		// 字段截取起点
		int startIndex = 0;

		// 缓存截取的字段值
		List<String> values = new ArrayList<>();
		for (int i = 0; i < chars.length; i++) {
			try {
				// 根据前面的状态推算当前状态
				Status currentStatus = processStatus(previousStatus, getCharType(chars[i], delimiter));

				// 如果前一个字符是双引号且双引号是值的左区间，则记录字段开始索引值
				if ((currentStatus == Status.QUOT_DATA || currentStatus == Status.QUOT_ORDINARY_CHECK)
					&& previousStatus == Status.QUOT_DATA_START) {
					startIndex = i;
				}

				// 如果前一个字符是两个双引号之间的内容，且当前字符是结束的双引号，则则添加开始值到当前索引前一个字符到缓存
				if (currentStatus == Status.QUOT_DATA_END && previousStatus == Status.QUOT_DATA) {
					values.add(handleBackslash(new String(chars, startIndex, i - startIndex), delimiter));
					startIndex = i + 1;
				}

				// 如果当前是分隔符，前一个字符是数据，则添加开始值到当前索引前一个字符到缓存
				if (currentStatus == Status.DATA_DELIMIT) {
					if (previousStatus != Status.QUOT_DATA_END) {
						values.add(handleBackslash(new String(chars, startIndex, i - startIndex), delimiter));
					}
					startIndex = i + 1;
				}
				previousStatus = currentStatus;
			} catch (IllegalStateException illegalStateException) {
				LOGGER.error("Invalid line data, error position:{}", i);
				return Collections.emptyList();
			}
		}

		// 因为一行内容没有结束符号，所以这里手动调一次结束符号状态变更
		Status lastStatus = processStatus(previousStatus, LINE_BREAK);

		// 如果最后的状态不是结束，则说明解析格式出错了
		if (lastStatus != Status.END) {
			LOGGER.error("Invalid line data:{}", lineContent);
			return Collections.emptyList();
		}

		// 如果最后一个字段内容不是双引号内的内容，需要单独处理添加到缓存
		if (previousStatus == Status.NO_QUOT_DATA) {
			values.add(handleBackslash(new String(chars, startIndex, chars.length - startIndex), delimiter));
		}

		// 如果最后一个字符是分隔符，还需要添加一个空串到缓存
		if (previousStatus == Status.DATA_DELIMIT) {
			values.add("");
		}
		return values;
	}

	/**
	 * 当数据成功截取之后，把截取的字符串中的转义字符替换为对应的字符
	 *
	 * @param value     需要转义的字符串
	 * @param delimiter 分隔符
	 * @return 最终的结果为字符串中的转义字符替换为对应的字符
	 */
	public String handleBackslash(String value, char delimiter) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}
		String handleResultValue = value.replace("\\" + delimiter, String.valueOf(delimiter));
		handleResultValue = handleResultValue.replace("\\\\", "\\");
		handleResultValue = handleResultValue.replace("\\\"", "\"");
		return handleResultValue;
	}

	/**
	 * 获取字符分类类型
	 *
	 * @param c         字符
	 * @param delimiter 分隔符，如果等于分隔符就是分隔符类型
	 * @return 字符分类类型
	 */
	public CharType getCharType(char c, char delimiter) {
		if (c == '"') {
			return QUOT;
		}
		if (c == '\\') {
			return BACKSLASH;
		}
		if (c == delimiter) {
			return DELIMITER;
		}
		return COMMON;
	}

	/**
	 * 字符状态驱动方法，根据当前的字符类型和状态，转换下一个状态
	 *
	 * @param previousStatus  上一个字符处理之后的状态，也可以理解为当前状态
	 * @param currentCharType 当前字符类型
	 * @return 当前字符处理之后的状态
	 */
	public Status processStatus(Status previousStatus, CharType currentCharType) {
		switch (previousStatus) {
			case START:
				return getStartNextStatus(currentCharType);
			case QUOT_DATA_START:
				return getQuotStartNextStatus(currentCharType);
			case QUOT_DATA:
				return getQuotDataNextStatus(currentCharType);
			case QUOT_DATA_END:
				return getQuotDataEndNextStatus(currentCharType);
			case NO_QUOT_DATA:
				return getNoQuotDataNextStatus(currentCharType);
			case DATA_DELIMIT:
				return getDataDelimitNextStatus(currentCharType);
			case QUOT_ORDINARY_CHECK:
				return getQuotOrdinaryCheckNextStatus(currentCharType);
			case NO_QUOT_ORDINARY_CHECK:
				return getNoQuotOrdinaryCheckNextStatus(currentCharType);
			case END:
				throw new FunctionException("The end.");
		}
		return null;
	}

	/**
	 * 开始状态下处理当前字符类型，会转变成什么状态
	 *
	 * @param charType 字符类型
	 * @return 转变之后的状态
	 */
	public Status getStartNextStatus(CharType charType) {
		switch (charType) {
			case COMMON:
				return Status.NO_QUOT_DATA;
			case QUOT:
				return Status.QUOT_DATA_START;
			case DELIMITER:
				return Status.DATA_DELIMIT;
			case BACKSLASH:
				return Status.NO_QUOT_ORDINARY_CHECK;
			default:
				throw new FunctionException("Unexpected state: " + charType);
		}
	}

	/**
	 * 双引号数据开始时状态下处理当前字符类型，会转变成什么状态
	 *
	 * @param charType 字符类型
	 * @return 转变之后的状态
	 */
	public Status getQuotStartNextStatus(CharType charType) {
		switch (charType) {
			case QUOT:
				return Status.QUOT_DATA_END;
			case BACKSLASH:
				return Status.QUOT_ORDINARY_CHECK;
			case DELIMITER:
			case COMMON:
				return Status.QUOT_DATA;
			default:
				throw new FunctionException("Unexpected state: " + charType);
		}
	}

	/**
	 * 双引号字符串中，遇到反斜线时处理当前字符类型，会转变成什么状态
	 *
	 * @param charType 字符类型
	 * @return 转变之后的状态
	 */
	public Status getQuotOrdinaryCheckNextStatus(CharType charType) {
		return Status.QUOT_DATA;
	}

	/**
	 * 处理双引号中数据字符时当前字符类型，会转变成什么状态
	 *
	 * @param charType 字符类型
	 * @return 转变之后的状态
	 */
	public Status getQuotDataNextStatus(CharType charType) {
		switch (charType) {
			case COMMON:
			case DELIMITER:
				return Status.QUOT_DATA;
			case QUOT:
				return Status.QUOT_DATA_END;
			case BACKSLASH:
				return Status.QUOT_ORDINARY_CHECK;
			default:
				throw new FunctionException("Unexpected state: " + charType);
		}
	}

	/**
	 * 双引号数据中结束双引号时处理当前字符类型，会转变成什么状态
	 *
	 * @param charType 字符类型
	 * @return 转变之后的状态
	 */
	public Status getQuotDataEndNextStatus(CharType charType) {
		switch (charType) {
			case DELIMITER:
				return Status.DATA_DELIMIT;
			case LINE_BREAK:
				return Status.END;
			default:
				throw new FunctionException("Unexpected state: " + charType);
		}
	}

	/**
	 * 处理非双引号包括的数据字符时，下一个转变的状态
	 *
	 * @param charType 字符类型
	 * @return 转变之后的状态
	 */
	public Status getNoQuotDataNextStatus(CharType charType) {
		switch (charType) {
			case COMMON:
				return Status.NO_QUOT_DATA;
			case QUOT:
				return Status.QUOT_DATA_START;
			case DELIMITER:
				return Status.DATA_DELIMIT;
			case BACKSLASH:
				return Status.NO_QUOT_ORDINARY_CHECK;
			case LINE_BREAK:
				return Status.END;
			default:
				throw new FunctionException("Unexpected state: " + charType);
		}
	}

	/**
	 * 非双引号内容时，处理反斜线
	 *
	 * @param charType 字符类型
	 * @return 转变之后的状态
	 */
	public Status getNoQuotOrdinaryCheckNextStatus(CharType charType) {
		return Status.NO_QUOT_DATA;
	}

	/**
	 * 遇到分隔符时，处理前字符类型，会转变成什么状态
	 *
	 * @param charType 字符类型
	 * @return 转变之后的状态
	 */
	public Status getDataDelimitNextStatus(CharType charType) {
		switch (charType) {
			case COMMON:
				return Status.NO_QUOT_DATA;
			case DELIMITER:
				return Status.DATA_DELIMIT;
			case QUOT:
				return Status.QUOT_DATA_START;
			case BACKSLASH:
				return Status.NO_QUOT_ORDINARY_CHECK;
			case LINE_BREAK:
				return Status.END;
			default:
				throw new FunctionException("Unexpected state: " + charType);
		}
	}
}
