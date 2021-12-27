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

import com.huawei.test.configelement.config.ParameterizedConfig;
import com.huawei.test.configelement.enums.SharingMode;
import com.huawei.test.exception.FunctionException;
import net.grinder.common.GrinderProperties;
import net.grinder.script.Grinder;
import net.grinder.script.InternalScriptContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvParameterizedTest {
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

	@Test(expected = FunctionException.class)
	public void test_initConfig_when_config_is_null() {
		CsvParameterized csvParameterized = new CsvParameterized();
		csvParameterized.initConfig(null);
	}

	@Test(expected = FunctionException.class)
	public void test_initConfig_when_parameterized_file_in_config_is_null() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile(null)
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.ALL_THREADS)
			.build();
		csvParameterized.initConfig(config);
	}

	@Test(expected = FunctionException.class)
	public void test_initConfig_when_sharing_mode_in_config_is_null() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(null)
			.build();
		csvParameterized.initConfig(config);
	}

	@Test(expected = FunctionException.class)
	public void test_initConfig_when_delimiter_in_config_is_null() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(null)
			.setSharingMode(SharingMode.ALL_THREADS)
			.build();
		csvParameterized.initConfig(config);
	}

	@Test(expected = FunctionException.class)
	public void test_initConfig_when_allow_quot_data_and_delimiter_size_gt_1() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter("@@")
			.setSharingMode(SharingMode.ALL_THREADS)
			.setAllowQuotedData(true)
			.build();
		csvParameterized.initConfig(config);
	}

	@Test(expected = FunctionException.class)
	public void test_hasNext_when_config_is_invalid() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(null)
			.setSharingMode(SharingMode.ALL_THREADS)
			.build();
		csvParameterized.initConfig(config);
		Assert.assertFalse(csvParameterized.hasNext());
	}

	@Test
	public void test_hasNext_when_recycle_on_eof() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.ALL_THREADS)
			.setRecycleOnEof(true)
			.build();
		csvParameterized.initConfig(config);
		Assert.assertTrue(csvParameterized.hasNext());
	}

	@Test
	public void test_hasNext_when_not_recycle_on_eof() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name");
		ParameterizedConfig config = new ParameterizedConfig.Builder().setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.ALL_THREADS)
			.setRecycleOnEof(false)
			.build();
		csvParameterized.initConfig(config);
		Assert.assertTrue(csvParameterized.hasNext());
	}

	@Test
	public void test_hasNext_when_not_recycle_on_eof_and_over_params_size() {
		CsvParameterized csvParameterized = new CsvParameterized();
		ParameterizedConfig config = new ParameterizedConfig.Builder()
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.ALL_THREADS)
			.setRecycleOnEof(false)
			.build();
		csvParameterized.initConfig(config);
		while (csvParameterized.hasNext()) {
			csvParameterized.nextLineValue();
		}
		Assert.assertFalse(csvParameterized.hasNext());
	}

	@Test
	public void test_nextLineValue_when_no_quot_data_and_not_set_variable_names_and_not_ignore_first_line() {
		CsvParameterized csvParameterized = new CsvParameterized();
		ParameterizedConfig config = new ParameterizedConfig.Builder()
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.ALL_THREADS)
			.setRecycleOnEof(false)
			.setAllowQuotedData(false)
			.setIgnoreFirstLine(false)
			.build();
		csvParameterized.initConfig(config);
		Assert.assertTrue(csvParameterized.hasNext());
		Map<String, String> paramsMap = csvParameterized.nextLineValue();
		Assert.assertEquals("lily8", paramsMap.get("name"));
		Assert.assertEquals("8", paramsMap.get("age"));
		Assert.assertEquals("北京8", paramsMap.get("address"));
	}

	@Test
	public void test_nextLineValue_when_no_quot_data_and_not_set_variable_names_and_ignore_first_line() {
		CsvParameterized csvParameterized = new CsvParameterized();
		ParameterizedConfig config = new ParameterizedConfig.Builder()
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.ALL_THREADS)
			.setRecycleOnEof(false)
			.setAllowQuotedData(false)
			.setIgnoreFirstLine(true)
			.build();
		csvParameterized.initConfig(config);
		Assert.assertTrue(csvParameterized.hasNext());
		Map<String, String> paramsMap = csvParameterized.nextLineValue();
		Assert.assertEquals("lily8", paramsMap.get("name"));
		Assert.assertEquals("8", paramsMap.get("age"));
		Assert.assertEquals("北京8", paramsMap.get("address"));
	}

	@Test
	public void test_nextLineValue_when_no_quot_data_and_set_variable_names_and_not_ignore_first_line() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name_define");
		variableNames.add("age_define");
		variableNames.add("phone_define");
		ParameterizedConfig config = new ParameterizedConfig.Builder()
			.setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.ALL_THREADS)
			.setRecycleOnEof(false)
			.setAllowQuotedData(false)
			.setIgnoreFirstLine(false)
			.build();
		csvParameterized.initConfig(config);
		Assert.assertTrue(csvParameterized.hasNext());
		Map<String, String> paramsMap = csvParameterized.nextLineValue();
		Assert.assertEquals("lily7", paramsMap.get("name_define"));
		Assert.assertEquals("7", paramsMap.get("age_define"));
		Assert.assertEquals("北京7", paramsMap.get("phone_define"));
	}

	@Test
	public void test_nextLineValue_when_no_quot_data_and_set_variable_names_and_ignore_first_line() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name_define");
		variableNames.add("age_define");
		variableNames.add("phone_define");
		ParameterizedConfig config = new ParameterizedConfig.Builder()
			.setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.ALL_THREADS)
			.setRecycleOnEof(false)
			.setAllowQuotedData(false)
			.setIgnoreFirstLine(true)
			.build();
		csvParameterized.initConfig(config);
		Assert.assertTrue(csvParameterized.hasNext());
		Map<String, String> paramsMap = csvParameterized.nextLineValue();
		Assert.assertEquals("lily8", paramsMap.get("name_define"));
		Assert.assertEquals("8", paramsMap.get("age_define"));
		Assert.assertEquals("北京8", paramsMap.get("phone_define"));
	}

	@Test
	public void test_nextLineValue_when_recycle_on_eof() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name_define");
		variableNames.add("age_define");
		variableNames.add("phone_define");
		ParameterizedConfig config = new ParameterizedConfig.Builder()
			.setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.ALL_THREADS)
			.setRecycleOnEof(true)
			.setAllowQuotedData(false)
			.setIgnoreFirstLine(true)
			.build();
		csvParameterized.initConfig(config);
		Assert.assertTrue(csvParameterized.hasNext());
		int runNumber = 0;
		while (csvParameterized.hasNext() && runNumber < 10) { // 只循环10次，相当于执行10次，因为recycleOnEof的hasNext不false
			Map<String, String> paramsMap = csvParameterized.nextLineValue();
			if (runNumber % 2 == 0) {
				Assert.assertEquals("lily8", paramsMap.get("name_define"));
				Assert.assertEquals("8", paramsMap.get("age_define"));
				Assert.assertEquals("北京8", paramsMap.get("phone_define"));
			} else {
				Assert.assertEquals("lily16", paramsMap.get("name_define"));
				Assert.assertEquals("16", paramsMap.get("age_define"));
				Assert.assertEquals("北京16", paramsMap.get("phone_define"));
			}
			runNumber++;
		}
	}

	@Test
	public void test_nextLineValue_when_use_quot_data() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name_define");
		variableNames.add("age_define");
		variableNames.add("phone_define");
		ParameterizedConfig config = new ParameterizedConfig.Builder()
			.setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration_quot_data.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.ALL_THREADS)
			.setRecycleOnEof(true)
			.setAllowQuotedData(true)
			.setIgnoreFirstLine(true)
			.build();
		csvParameterized.initConfig(config);
		Assert.assertTrue(csvParameterized.hasNext());
		int runNumber = 0;
		while (csvParameterized.hasNext() && runNumber < 10) { // 只循环10次，相当于执行10次，因为recycleOnEof的hasNext不false
			Map<String, String> paramsMap = csvParameterized.nextLineValue();
			if (runNumber % 2 == 0) {
				Assert.assertEquals("lily8", paramsMap.get("name_define"));
				Assert.assertEquals("8", paramsMap.get("age_define"));
				Assert.assertEquals("北京,海淀区\"小区\",中关村8", paramsMap.get("phone_define"));
			} else {
				Assert.assertEquals("lily16", paramsMap.get("name_define"));
				Assert.assertEquals("16", paramsMap.get("age_define"));
				Assert.assertEquals("北京,海淀区\"小区\",中关村16", paramsMap.get("phone_define"));
			}
			runNumber++;
		}
	}

	@Test
	public void test_nextLineValue_when_use_current_agent_mode() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> variableNames = new ArrayList<>();
		variableNames.add("name_define");
		variableNames.add("age_define");
		variableNames.add("phone_define");
		ParameterizedConfig config = new ParameterizedConfig.Builder()
			.setParameterizedNames(variableNames)
			.setParameterizedFile("/params_2_iteration_quot_data.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.CURRENT_AGENT)
			.setRecycleOnEof(true)
			.setAllowQuotedData(true)
			.setIgnoreFirstLine(true)
			.build();
		csvParameterized.initConfig(config);
		Assert.assertTrue(csvParameterized.hasNext());
		int runNumber = 0;
		while (csvParameterized.hasNext() && runNumber < 4) { // 只循环2次，相当于执行2次，因为recycleOnEof的hasNext不false
			Map<String, String> paramsMap = csvParameterized.nextLineValue();
			if (runNumber == 0) {
				Assert.assertEquals("lily4", paramsMap.get("name_define"));
				Assert.assertEquals("4", paramsMap.get("age_define"));
				Assert.assertEquals("北京4", paramsMap.get("phone_define"));
			} else if (runNumber == 1) {
				Assert.assertEquals("lily8", paramsMap.get("name_define"));
				Assert.assertEquals("8", paramsMap.get("age_define"));
				Assert.assertEquals("北京,海淀区\"小区\",中关村8", paramsMap.get("phone_define"));
			} else if (runNumber == 2) {
				Assert.assertEquals("lily12", paramsMap.get("name_define"));
				Assert.assertEquals("12", paramsMap.get("age_define"));
				Assert.assertEquals("北京12", paramsMap.get("phone_define"));
			} else {
				Assert.assertEquals("lily16", paramsMap.get("name_define"));
				Assert.assertEquals("16", paramsMap.get("age_define"));
				Assert.assertEquals("北京,海淀区\"小区\",中关村16", paramsMap.get("phone_define"));
			}
			runNumber++;
		}
	}

	@Test
	public void test_nextLineValue_when_use_current_process_mode() {
		CsvParameterized csvParameterized = new CsvParameterized();
		ParameterizedConfig config = new ParameterizedConfig.Builder()
			.setParameterizedFile("/params_2_iteration_quot_data.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.CURRENT_PROCESS)
			.setRecycleOnEof(true)
			.setAllowQuotedData(true)
			.setIgnoreFirstLine(true)
			.build();
		csvParameterized.initConfig(config);
		Assert.assertTrue(csvParameterized.hasNext());
		int runNumber = 0;
		while (csvParameterized.hasNext() && runNumber < 4) { // 只循环2次，相当于执行2次，因为recycleOnEof的hasNext不false
			Map<String, String> paramsMap = csvParameterized.nextLineValue();
			if (runNumber == 0) {
				Assert.assertEquals("lily2", paramsMap.get("name"));
				Assert.assertEquals("2", paramsMap.get("age"));
				Assert.assertEquals("北京2", paramsMap.get("address"));
			} else if (runNumber == 1) {
				Assert.assertEquals("lily4", paramsMap.get("name"));
				Assert.assertEquals("4", paramsMap.get("age"));
				Assert.assertEquals("北京4", paramsMap.get("address"));
			} else if (runNumber == 2) {
				Assert.assertEquals("lily6", paramsMap.get("name"));
				Assert.assertEquals("6", paramsMap.get("age"));
				Assert.assertEquals("北京6", paramsMap.get("address"));
			} else {
				Assert.assertEquals("lily8", paramsMap.get("name"));
				Assert.assertEquals("8", paramsMap.get("age"));
				Assert.assertEquals("北京,海淀区\"小区\",中关村8", paramsMap.get("address"));
			}
			runNumber++;
		}
	}

	@Test
	public void test_nextLineValue_when_use_current_thread_mode() {
		CsvParameterized csvParameterized = new CsvParameterized();
		ParameterizedConfig config = new ParameterizedConfig.Builder()
			.setParameterizedFile("/params_2_iteration_quot_data.csv")
			.setParameterizedDelimiter(",")
			.setSharingMode(SharingMode.CURRENT_THREAD)
			.setRecycleOnEof(true)
			.setAllowQuotedData(true)
			.setIgnoreFirstLine(true)
			.build();
		csvParameterized.initConfig(config);
		Assert.assertTrue(csvParameterized.hasNext());
		int runNumber = 0;
		while (csvParameterized.hasNext()) { // 只循环2次，相当于执行2次，因为recycleOnEof的hasNext不false
			Map<String, String> paramsMap = csvParameterized.nextLineValue();
			if (runNumber == 0) {
				Assert.assertEquals("lily1", paramsMap.get("name"));
				Assert.assertEquals("1", paramsMap.get("age"));
				Assert.assertEquals("北京1", paramsMap.get("address"));
			} else if (runNumber == 1) {
				Assert.assertEquals("lily2", paramsMap.get("name"));
				Assert.assertEquals("2", paramsMap.get("age"));
				Assert.assertEquals("北京2", paramsMap.get("address"));
			} else if (runNumber == 2) {
				Assert.assertEquals("lily3", paramsMap.get("name"));
				Assert.assertEquals("3", paramsMap.get("age"));
				Assert.assertEquals("北京3", paramsMap.get("address"));
			}
			if(runNumber == 16) { // 第二轮开始
				Assert.assertEquals("lily1", paramsMap.get("name"));
				Assert.assertEquals("1", paramsMap.get("age"));
				Assert.assertEquals("北京1", paramsMap.get("address"));
			}
			if(runNumber == 17) { // 第二轮开始
				Assert.assertEquals("lily2", paramsMap.get("name"));
				Assert.assertEquals("2", paramsMap.get("age"));
				Assert.assertEquals("北京2", paramsMap.get("address"));
				break;
			}
			runNumber++;
		}
	}

	@Test
	public void test_splitLineContent_when_no_quot_data() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> values = csvParameterized.splitLineContent("aaa,bbb,ccc,,", ",", false);
		Assert.assertEquals(5, values.size());
		Assert.assertEquals("aaa", values.get(0));
		Assert.assertEquals("bbb", values.get(1));
		Assert.assertEquals("ccc", values.get(2));
		Assert.assertEquals("", values.get(3));
		Assert.assertEquals("", values.get(4));
	}

	@Test
	public void test_splitLineContent_when_has_quot_data() {
		CsvParameterized csvParameterized = new CsvParameterized();
		List<String> values = csvParameterized.splitLineContent("aaa,\"b,bb\",ccc,,", ",", true);
		Assert.assertEquals(5, values.size());
		Assert.assertEquals("aaa", values.get(0));
		Assert.assertEquals("b,bb", values.get(1));
		Assert.assertEquals("ccc", values.get(2));
		Assert.assertEquals("", values.get(3));
		Assert.assertEquals("", values.get(4));
	}
}
