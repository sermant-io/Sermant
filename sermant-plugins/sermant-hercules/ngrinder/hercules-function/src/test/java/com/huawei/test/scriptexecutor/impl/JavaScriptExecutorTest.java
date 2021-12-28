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
import org.junit.Assert;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JavaScriptExecutorTest {
	@Test(expected = FunctionException.class)
	public void test_executeScript_by_stringScript_when_scriptContent_is_empty() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		scriptExecutor.executeScript("", new HashMap<>());
	}

	@Test
	public void test_executeScript_by_stringScript_when_scriptContent_no_constValues() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		Object result = scriptExecutor.executeScript("2+3", new HashMap<>());
		Assert.assertEquals(5, result);
	}

	@Test
	public void test_executeScript_by_stringScript_when_scriptContent_has_constValues() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		String scriptContent = "date.getTime()";
		Map<String, Object> constValues = new HashMap<>();
		Date date = new Date();
		constValues.put("date", date);
		Object result = scriptExecutor.executeScript(scriptContent, constValues);
		Assert.assertEquals(date.getTime(), result);
	}

	@Test(expected = FunctionException.class)
	public void test_executeScript_by_stringScript_when_scriptContent_is_invalid() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		String scriptContent = "aa+bb";
		scriptExecutor.executeScript(scriptContent, new HashMap<>());
	}

	@Test(expected = FunctionException.class)
	public void test_executeScript_by_reader_when_scriptContent_is_null() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		scriptExecutor.executeScript((Reader) null, new HashMap<>());
	}

	@Test
	public void test_executeScript_by_reader_when_scriptContent_no_constValues() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		Object result = scriptExecutor.executeScript(new StringReader("2+3"), new HashMap<>());
		Assert.assertEquals(5, result);
	}

	@Test
	public void test_executeScript_by_reader_when_scriptContent_has_constValues() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		String scriptContent = "date.getTime()";
		Map<String, Object> constValues = new HashMap<>();
		Date date = new Date();
		constValues.put("date", date);
		Object result = scriptExecutor.executeScript(new StringReader(scriptContent), constValues);
		Assert.assertEquals(date.getTime(), result);
	}

	@Test(expected = FunctionException.class)
	public void test_executeScript_by_reader_when_scriptContent_is_invalid() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		String scriptContent = "aa+bb";
		scriptExecutor.executeScript(new StringReader(scriptContent), new HashMap<>());
	}

	@Test(expected = FunctionException.class)
	public void test_executeScriptFunction_when_functionCode_is_null() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		scriptExecutor.executeScriptFunction(null, "getTime", new HashMap<>(), new Object() {
		});
	}

	@Test(expected = FunctionException.class)
	public void test_executeScriptFunction_when_functionCode_is_empty() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		scriptExecutor.executeScriptFunction(null, "getTime", new HashMap<>(), new Object() {
		});
	}

	@Test(expected = FunctionException.class)
	public void test_executeScriptFunction_when_functionName_is_empty() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		String functionCode = "function getTime(){ return date.getTime();}";
		scriptExecutor.executeScriptFunction(functionCode, "", new HashMap<>(), new Object() {
		});
	}

	@Test(expected = FunctionException.class)
	public void test_executeScriptFunction_when_functionName_is_null() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		String functionCode = "function getTime(){ return date.getTime();}";
		scriptExecutor.executeScriptFunction(functionCode, null, new HashMap<>(), new Object() {
		});
	}

	@Test
	public void test_executeScriptFunction_when_constValue_is_null() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		String functionCode = "function add(){ return 2+3;}";
		scriptExecutor.executeScriptFunction(functionCode, "add", null, new Object() {
		});
	}

	@Test
	public void test_executeScriptFunction_when_constValue_is_empty() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		String functionCode = "function add(){ return 2+3;}";
		scriptExecutor.executeScriptFunction(functionCode, "add", new HashMap<>(), new Object() {
		});
	}

	@Test
	public void test_executeScriptFunction_when_functionParams_is_null() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		String functionCode = "function add(){ return 2+3;}";
		Object addResult = scriptExecutor.executeScriptFunction(functionCode, "add", new HashMap<>(), (Object[]) null);
		Assert.assertEquals(5, addResult);
	}

	@Test
	public void test_executeScriptFunction_when_has_constValues_and_functionParams() {
		ScriptExecutor scriptExecutor = new JavaScriptExecutor();
		String functionCode = "function add(a,b){ return a+b+c;}";
		Map<String, Object> constValues = new HashMap<>();
		constValues.put("c", 2);
		Object addResult = scriptExecutor.executeScriptFunction(functionCode, "add", constValues, 2, 3);
		Assert.assertEquals(7.0, addResult);
	}
}
