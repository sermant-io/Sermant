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
import com.huawei.test.configelement.enums.SharingMode;
import com.huawei.test.configelement.impl.CsvParameterized;
import net.grinder.common.GrinderProperties;
import net.grinder.script.Grinder;
import net.grinder.script.InternalScriptContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class BaseParameterizedTest {
	@Before
	public void init() {
		InternalScriptContext internalScriptContext = Mockito.mock(InternalScriptContext.class);
		GrinderProperties grinderProperties = new GrinderProperties();
		grinderProperties.put("grinder.agents", 2);
		grinderProperties.put("grinder.processes", 2);
		grinderProperties.put("grinder.threads", 2);
		Mockito.when(internalScriptContext.getProperties()).thenReturn(grinderProperties);
		Mockito.when(internalScriptContext.getAgentNumber()).thenReturn(1);
		Mockito.when(internalScriptContext.getProcessNumber()).thenReturn(1);
		Mockito.when(internalScriptContext.getThreadNumber()).thenReturn(1);
		Mockito.when(internalScriptContext.getRunNumber()).thenReturn(3);
		Grinder.grinder = internalScriptContext;
	}

	@Test
	public void test_readLines_when_parameterized_file_is_null() {
		BaseParameterized csvParameterized = new CsvParameterized();
		List<String> strings = csvParameterized.readLines(null);
		Assert.assertTrue(strings.isEmpty());
	}

	@Test
	public void test_readLines_when_parameterized_file_not_exist() {
		BaseParameterized csvParameterized = new CsvParameterized();
		List<String> strings = csvParameterized.readLines("/notExist.csv");
		Assert.assertTrue(strings.isEmpty());
	}

	@Test
	public void test_readLines_when_parameterized_file_exist() {
		BaseParameterized csvParameterized = new CsvParameterized();
		List<String> strings = csvParameterized.readLines("/params_2_iteration.csv");
		Assert.assertFalse(strings.isEmpty());
	}

	@Test
	public void test_getParamFileNextLineNumber_when_all_thread_sharing_mode_and_runNumber_3() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.ALL_THREADS)
			.setAllowQuotedData(true)
			.build();
		csvParameterized.initConfig(config);
		int paramFileNextLineNumber = csvParameterized.getParamFileNextLineNumber(3);
		Assert.assertEquals(31, paramFileNextLineNumber);
	}

	@Test
	public void test_getParamFileNextLineNumber_when_current_agent_sharing_mode_and_runNumber_3() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.CURRENT_AGENT)
			.setAllowQuotedData(true)
			.build();
		csvParameterized.initConfig(config);
		int paramFileNextLineNumber = csvParameterized.getParamFileNextLineNumber(3);
		Assert.assertEquals(15, paramFileNextLineNumber);

	}

	@Test
	public void test_getParamFileNextLineNumber_when_current_process_sharing_mode_and_runNumber_3() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.CURRENT_PROCESS)
			.setAllowQuotedData(true)
			.build();
		csvParameterized.initConfig(config);
		int paramFileNextLineNumber = csvParameterized.getParamFileNextLineNumber(3);
		Assert.assertEquals(7, paramFileNextLineNumber);
	}

	@Test
	public void test_getParamFileNextLineNumber_when_current_thread_sharing_mode_and_runNumber_3() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.CURRENT_THREAD)
			.setAllowQuotedData(true)
			.build();
		csvParameterized.initConfig(config);
		int paramFileNextLineNumber = csvParameterized.getParamFileNextLineNumber(3);
		Assert.assertEquals(3, paramFileNextLineNumber);
	}

	@Test
	public void test_getParamFileNextLineNumber_when_all_thread_sharing_mode_and_runNumber_0() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.ALL_THREADS)
			.setAllowQuotedData(true)
			.build();
		csvParameterized.initConfig(config);
		int paramFileNextLineNumber = csvParameterized.getParamFileNextLineNumber(0);
		Assert.assertEquals(7, paramFileNextLineNumber);
	}

	@Test
	public void test_getParamFileNextLineNumber_when_current_agent_sharing_mode_and_runNumber_0() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.CURRENT_AGENT)
			.setAllowQuotedData(true)
			.build();
		csvParameterized.initConfig(config);
		int paramFileNextLineNumber = csvParameterized.getParamFileNextLineNumber(0);
		Assert.assertEquals(3, paramFileNextLineNumber);

	}

	@Test
	public void test_getParamFileNextLineNumber_when_current_process_sharing_mode_and_runNumber_0() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.CURRENT_PROCESS)
			.setAllowQuotedData(true)
			.build();
		csvParameterized.initConfig(config);
		int paramFileNextLineNumber = csvParameterized.getParamFileNextLineNumber(0);
		Assert.assertEquals(1, paramFileNextLineNumber);
	}

	@Test
	public void test_getParamFileNextLineNumber_when_current_thread_sharing_mode_and_runNumber_0() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.CURRENT_THREAD)
			.setAllowQuotedData(true)
			.build();
		csvParameterized.initConfig(config);
		int paramFileNextLineNumber = csvParameterized.getParamFileNextLineNumber(0);
		Assert.assertEquals(0, paramFileNextLineNumber);
	}
}
