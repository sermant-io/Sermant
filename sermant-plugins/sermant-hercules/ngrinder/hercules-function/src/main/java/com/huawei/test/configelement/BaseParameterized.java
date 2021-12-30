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

import com.huawei.test.configelement.config.ParameterizedConfig;
import com.huawei.test.configelement.service.ExecuteTimesInfo;
import com.huawei.test.exception.FunctionException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 功能描述：参数化数据源配置
 *
 * @author zl
 * @since 2021-12-08
 */
public abstract class BaseParameterized extends ConfigElement<ParameterizedConfig> implements IParameterized {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseParameterized.class);

	/**
	 * 缓存各个线程自己的参数化分配结果
	 */
	private final List<List<String>> currentThreadParams = new ArrayList<>();

	/**
	 * 参数化配置
	 */
	private ParameterizedConfig config;

	/**
	 * 参数化名称
	 */
	private final List<String> parameterizedNames = new ArrayList<>();

	/**
	 * 参数取值游标
	 */
	private int lineCursor;

	/**
	 * 配置是否合法
	 */
	private boolean isConfigValid = false;


	@Override
	public void initConfig(ParameterizedConfig config) {
		this.config = config;
		if (!isConfigValid()) {
			throw new FunctionException("Error config for parameterized.");
		}
		isConfigValid = true;

		// 获取参数化文件内容
		List<String> fileLines = readLines(config.getParameterizedFile());
		if (fileLines == null || fileLines.isEmpty()) {
			throw new FunctionException("Get the content of parameterized file failed.");
		}

		// 初始化当前线程能使用的参数化文件中的值
		String parameterizedDelimiter = config.getParameterizedDelimiter();
		boolean allowQuotedData = config.isAllowQuotedData();
		boolean ignoreFirstLine = config.isIgnoreFirstLine();

		// 初始化参数名称列表
		if (config.getParameterizedNames() == null || config.getParameterizedNames().size() == 0) {
			// 如果配置文件没有传入参数化名称，就从第一行文件获取
			parameterizedNames.addAll(splitLineContent(fileLines.get(0), parameterizedDelimiter, allowQuotedData));
			ignoreFirstLine = true;
		} else {
			// 如果传入了参数化名称，则直接使用
			parameterizedNames.addAll(config.getParameterizedNames());
		}

		// 初始化参数
		int runNumber = 0;
		int lineNumber = getParamFileNextLineNumber(runNumber);
		while (lineNumber < fileLines.size()) {
			if (ignoreFirstLine && (lineNumber == fileLines.size() - 1)) {
				break;
			}
			String lineContent = fileLines.get(ignoreFirstLine ? lineNumber + 1 : lineNumber);

			// 根据分隔符分割一行数据
			List<String> values = splitLineContent(lineContent, parameterizedDelimiter, allowQuotedData);
			currentThreadParams.add(values);

			// 获取当前线程下一行使用的参数所在行号
			runNumber++;
			lineNumber = getParamFileNextLineNumber(runNumber);
		}
	}

	@Override
	public boolean isConfigValid() {
		if (config == null) {
			LOGGER.error("The config for parameterized is null.");
			return false;
		}
		if (StringUtils.isEmpty(config.getParameterizedFile())) {
			LOGGER.error("The data file path for parameterized is null.");
			return false;
		}
		if (config.getSharingMode() == null) {
			LOGGER.error("The sharing mode for parameterized is null.");
			return false;
		}
		if (StringUtils.isEmpty(config.getParameterizedDelimiter())) {
			LOGGER.error("The delimiter for parameterized is null.");
			return false;
		}
		String parameterizedDelimiter = config.getParameterizedDelimiter();
		if (config.isAllowQuotedData() && parameterizedDelimiter.length() > 1) {
			LOGGER.error("The delimiter length great than 1 when using quoted data.");
			return false;
		}
		return true;
	}

	/**
	 * 是否还有下一个数据，这里可能会根据recycleOnEof参数影响，如果打开这个，就会一直返回true
	 *
	 * @return true：有，false：没有
	 */
	@Override
	public boolean hasNext() {
		if (!isConfigValid) {
			return false;
		}
		if (currentThreadParams.size() == 0) {
			return false;
		}
		if (config.isRecycleOnEof()) {
			return true;
		}
		return lineCursor < currentThreadParams.size();
	}

	/**
	 * 当有下一个数据时，返回下一行所有的数据，这个值在参数化文件中的位置是经过参数化中sharingMode来决定的
	 *
	 * @return 根据SharingMode计算出来的下一行的所有参数化取值
	 */
	@Override
	public Map<String, String> nextLineValue() {
		if (!hasNext()) {
			throw new FunctionException("Current thread can not match any params.");
		}
		if (parameterizedNames.size() == 0) {
			throw new FunctionException("The parameterized names hadn't been init.");
		}
		List<String> values = currentThreadParams.get(lineCursor);
		if (values == null || values.isEmpty()) {
			throw new FunctionException("Split param line failed.");
		}

		// 参数名称的个数和参数值的个数必须是一致的
		if (parameterizedNames.size() != values.size()) {
			throw new FunctionException("The count of variable values didn't match the count of variable names.");
		}
		Map<String, String> nextValue = new HashMap<>();
		for (int i = 0; i < parameterizedNames.size(); i++) {
			nextValue.put(parameterizedNames.get(i), values.get(i));
		}

		// 游标指向下一行数据
		lineCursor++;
		if (config.isRecycleOnEof() && lineCursor >= currentThreadParams.size()) {
			lineCursor = 0;
		}
		return nextValue;
	}

	/**
	 * 获取当前线程执行时可拿到的参数化文件的索引，这个索引是从0开始的
	 *
	 * @param runNumber 执行序号
	 * @return 从0开始的当前线程可用的参数化文件索引
	 */
	protected int getParamFileNextLineNumber(int runNumber) {
		if (!isConfigValid) {
			LOGGER.error("Config is invalid.");
			throw new FunctionException("Config is invalid.");
		}
		ExecuteTimesInfo executeTimesInfo = getExecuteTimesInfo(runNumber);
		return config.getSharingMode().getGrinderCountService().nextIncrementNumber(executeTimesInfo);
	}


	/**
	 * 读取文件的每一行数据放到list集合里面
	 *
	 * @param parameterizedFilePath 参数化文件路径
	 * @return 每一行数据的list
	 */
	protected List<String> readLines(String parameterizedFilePath) {
		if (StringUtils.isEmpty(parameterizedFilePath)) {
			LOGGER.error("The parameterized file path is empty.");
			return Collections.emptyList();
		}
		try (InputStream parameterizedResource = getClass().getResourceAsStream(parameterizedFilePath)) {
			if (parameterizedResource == null) {
				LOGGER.error("Invalid parameterized file path.");
				return Collections.emptyList();
			}
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(parameterizedResource, StandardCharsets.UTF_8))) {
				return bufferedReader.lines().collect(Collectors.toList());
			}
		} catch (IOException e) {
			LOGGER.error("Open parameterized file fail, reason:{}", e.getMessage());
			return Collections.emptyList();
		}
	}

	/**
	 * 拆分一行文件内容，根据分隔符拆分成字段值列表
	 *
	 * @param lineContent 文件内容
	 * @param delimiter   文件分隔符
	 * @param quotData    是否使用双引号引用数据
	 * @return 字段值列表
	 */
	protected abstract List<String> splitLineContent(String lineContent, String delimiter, boolean quotData);
}
