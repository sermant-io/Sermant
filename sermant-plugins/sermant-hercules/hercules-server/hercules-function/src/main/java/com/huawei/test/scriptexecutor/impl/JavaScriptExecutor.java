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

import com.huawei.test.scriptexecutor.ScriptExecutor;

import javax.script.ScriptException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述：javascript执行器
 *
 * @author zl
 * @since 2021-12-10
 */
public class JavaScriptExecutor<T> implements ScriptExecutor<T> {
	@Override
	public T executeScript(String scriptContent, String scriptType, Map<String, Object> params) throws ScriptException, NoSuchMethodException {
		Map<String, Object> map = new HashMap<>();
		return null;
	}

	@Override
	public T executeScript(Reader scriptContent, String scriptType, Map<String, Object> params) throws ScriptException, NoSuchMethodException {
		return null;
	}
}
