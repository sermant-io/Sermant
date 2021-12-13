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

package com.huawei.test.scriptexecutor.impl;

import com.huawei.test.exception.FunctionException;
import com.huawei.test.scriptexecutor.ScriptExecutor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.Reader;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 功能描述：javascript执行器
 *
 * @author zl
 * @since 2021-12-10
 */
public class JavaScriptExecutor implements ScriptExecutor {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptExecutor.class);

	@Override
	public Object executeScript(String scriptContent, Map<String, Object> constValues) {
		if (StringUtils.isEmpty(scriptContent)) {
			LOGGER.error("The script content is empty.");
			throw new FunctionException("The script content is empty.");
		}
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");
		if (constValues != null && !constValues.isEmpty()) {
			constValues.forEach(engine::put);
		}
		try {
			return engine.eval(scriptContent);
		} catch (ScriptException e) {
			LOGGER.error("Occur an error when execute script.");
			throw new FunctionException("Occur an error when execute script.");
		}
	}

	@Override
	public Object executeScript(Reader scriptContent, Map<String, Object> constValues) {
		if (scriptContent == null) {
			LOGGER.error("The script content is empty.");
			throw new FunctionException("The script content is empty.");
		}
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");
		if (constValues != null && !constValues.isEmpty()) {
			constValues.forEach(engine::put);
		}
		try {
			return engine.eval(scriptContent);
		} catch (ScriptException e) {
			LOGGER.error("Occur an error when execute script by reader.");
			throw new FunctionException("Occur an error when execute script by reader.");
		}
	}

	@Override
	public Object executeScriptFunction(String functionCode, String functionName, Map<String, Object> constValues, Object... functionParams) {
		if (StringUtils.isEmpty(functionCode) || StringUtils.isEmpty(functionName)) {
			LOGGER.error("The script content or function name is empty.");
			throw new FunctionException("The script content or function name is empty.");
		}
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");
		if (constValues != null && !constValues.isEmpty()) {
			constValues.forEach(engine::put);
		}
		try {
			engine.eval(functionCode);
			Invocable invocable = (Invocable) engine;
			return invocable.invokeFunction(functionName, functionParams);
		} catch (ScriptException | NoSuchMethodException e) {
			LOGGER.error("Occur an error when execute script by function.");
			throw new FunctionException("Occur an error when execute script by function.");
		}
	}
}
