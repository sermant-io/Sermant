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

package com.huawei.test.scriptexecutor;

import javax.script.ScriptException;
import java.io.Reader;
import java.util.Map;

/**
 * 功能描述：脚本执行接口
 *
 * @author zl
 * @since 2021-12-10
 */
public interface ScriptExecutor {
	/**
	 * 执行字符串脚本
	 *
	 * @param scriptContent 脚本内容
	 * @param constValues 脚本种需要使用的参数
	 * @return 脚本执行结果
	 */
	Object executeScript(String scriptContent, Map<String, Object> constValues);

	/**
	 * 执行脚本，脚本的内容通过reader读入
	 *
	 * @param scriptContent 脚本内容
	 * @param constValues 脚本种需要使用的参数
	 * @return 脚本执行结果
	 */
	Object executeScript(Reader scriptContent, Map<String, Object> constValues);

	/**
	 * 执行函数脚本
	 *
	 * @param functionCode js函数代码
	 * @param functionName js函数名称
	 * @param constValues js函数中用到的常量
	 * @param functionParams js函数参数传入变量
	 * @return js函数执行结果
	 */
	Object executeScriptFunction(String functionCode, String functionName, Map<String, Object> constValues, Object ... functionParams);
}
