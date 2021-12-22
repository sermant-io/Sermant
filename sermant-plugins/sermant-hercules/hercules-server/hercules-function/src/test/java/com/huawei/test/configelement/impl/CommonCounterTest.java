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

import com.huawei.test.configelement.Counter;
import com.huawei.test.configelement.config.CounterConfig;
import com.huawei.test.configelement.enums.SharingMode;
import com.huawei.test.configelement.service.ExecuteTimesInfo;
import com.huawei.test.configelement.service.impl.AgentModeCountService;
import com.huawei.test.configelement.service.impl.AllThreadModeCountService;
import com.huawei.test.configelement.service.impl.CurrentThreadModeCountService;
import com.huawei.test.configelement.service.impl.ProcessModeCountService;
import com.huawei.test.exception.FunctionException;
import net.grinder.common.GrinderProperties;
import net.grinder.script.Grinder;
import net.grinder.script.InternalScriptContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CommonCounterTest {
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
	public void test_isConfigValid_when_config_is_null() {
		Counter counter = new CommonCounter();
		counter.initConfig(null);
		boolean configValid = counter.isConfigValid();
		Assert.assertFalse(configValid);
	}

	@Test
	public void test_isConfigValid_when_sharingMode_is_null() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(null)
			.setIncrement(1)
			.setMaxValue(100)
			.setStartValue(0)
			.build();
		Counter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		boolean configValid = counter.isConfigValid();
		Assert.assertFalse(configValid);
	}

	@Test
	public void test_isConfigValid_when_config_is_valid() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.ALL_THREADS)
			.setIncrement(1)
			.setMaxValue(100)
			.setStartValue(0)
			.build();
		Counter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		boolean configValid = counter.isConfigValid();
		Assert.assertTrue(configValid);
	}

	@Test(expected = FunctionException.class)
	public void test_nextNumber_when_config_is_null() {
		Counter counter = new CommonCounter();
		counter.initConfig(null);
		String nextNumber = counter.nextNumber();
		Assert.assertEquals("", nextNumber);
	}

	@Test(expected = FunctionException.class)
	public void test_nextNumber_when_sharingMode_is_null() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(null)
			.setIncrement(1)
			.setMaxValue(100)
			.setStartValue(0)
			.build();
		Counter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		String nextNumber = counter.nextNumber();
		Assert.assertEquals("", nextNumber);
	}

	@Test
	public void test_nextNumber_when_has_maxValue_and_lt_than_maxValue() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.ALL_THREADS)
			.setIncrement(1)
			.setMaxValue(100)
			.setStartValue(0)
			.build();
		Counter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		String nextNumber = counter.nextNumber();
		Assert.assertEquals("31", nextNumber);
	}

	@Test(expected = FunctionException.class)
	public void test_nextNumber_when_has_maxValue_and_gt_than_maxValue() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.ALL_THREADS)
			.setIncrement(1)
			.setMaxValue(2)
			.setStartValue(0)
			.build();
		Counter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		String nextNumber = counter.nextNumber();
		Assert.assertEquals("31", nextNumber);
	}

	@Test
	public void test_nextNumber_when_has_maxValue_and_eq_maxValue() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.ALL_THREADS)
			.setIncrement(1)
			.setMaxValue(31)
			.setStartValue(0)
			.build();
		Counter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		String nextNumber = counter.nextNumber();
		Assert.assertEquals("31", nextNumber);
	}

	@Test
	public void test_nextNumber_when_has_numberFormat() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.ALL_THREADS)
			.setIncrement(1)
			.setMaxValue(31)
			.setStartValue(0)
			.setNumberFormat("%d$")
			.build();
		Counter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		String nextNumber = counter.nextNumber();
		Assert.assertEquals("31$", nextNumber);
	}

	@Test
	public void test_hasNext_when_config_is_null() {
		Counter counter = new CommonCounter();
		counter.initConfig(null);
		boolean hasNext = counter.hasNext();
		Assert.assertFalse(hasNext);
	}

	@Test
	public void test_hasNext_when_sharingMode_is_null() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(null)
			.setIncrement(1)
			.setMaxValue(100)
			.setStartValue(0)
			.build();
		Counter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		boolean hasNext = counter.hasNext();
		Assert.assertFalse(hasNext);
	}

	@Test
	public void test_hasNext_when_has_maxValue_and_lt_than_maxValue() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.ALL_THREADS)
			.setIncrement(1)
			.setMaxValue(100)
			.setStartValue(0)
			.build();
		Counter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		boolean hasNext = counter.hasNext();
		Assert.assertTrue(hasNext);
	}

	@Test
	public void test_hasNext_when_has_maxValue_and_gt_than_maxValue() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.ALL_THREADS)
			.setIncrement(1)
			.setMaxValue(2)
			.setStartValue(0)
			.build();
		Counter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		boolean hasNext = counter.hasNext();
		Assert.assertFalse(hasNext);
	}

	@Test
	public void test_hasNext_when_has_maxValue_and_eq_maxValue() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.ALL_THREADS)
			.setIncrement(1)
			.setMaxValue(31)
			.setStartValue(0)
			.build();
		Counter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		boolean hasNext = counter.hasNext();
		Assert.assertTrue(hasNext);
	}

	@Test
	public void test_nextValue_when_all_thread_mode_when_increment_1() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.ALL_THREADS)
			.setIncrement(1)
			.setMaxValue(1000)
			.setStartValue(0)
			.build();
		CommonCounter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		Assert.assertEquals(31, counter.nextValue());
	}

	@Test
	public void test_nextValue_when_agent_mode_when_increment_1() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.CURRENT_AGENT)
			.setIncrement(1)
			.setMaxValue(1000)
			.setStartValue(0)
			.build();
		CommonCounter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		Assert.assertEquals(15, counter.nextValue());
	}

	@Test
	public void test_nextValue_when_process_mode_when_increment_1() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.CURRENT_PROCESS)
			.setIncrement(1)
			.setMaxValue(1000)
			.setStartValue(0)
			.build();
		CommonCounter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		Assert.assertEquals(7, counter.nextValue());
	}

	@Test
	public void test_nextValue_when_current_thread_mode_when_increment_1() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.CURRENT_THREAD)
			.setIncrement(1)
			.setMaxValue(1000)
			.setStartValue(0)
			.build();
		CommonCounter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		Assert.assertEquals(3, counter.nextValue());
	}

	@Test
	public void test_nextValue_when_all_thread_mode_when_increment_3() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.ALL_THREADS)
			.setIncrement(3)
			.setMaxValue(1000)
			.setStartValue(0)
			.build();
		CommonCounter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		Assert.assertEquals(93, counter.nextValue());
	}

	@Test
	public void test_nextValue_when_agent_mode_when_increment_3() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.CURRENT_AGENT)
			.setIncrement(3)
			.setMaxValue(1000)
			.setStartValue(0)
			.build();
		CommonCounter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		Assert.assertEquals(45, counter.nextValue());
	}

	@Test
	public void test_nextValue_when_process_mode_when_increment_3() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.CURRENT_PROCESS)
			.setIncrement(3)
			.setMaxValue(1000)
			.setStartValue(0)
			.build();
		CommonCounter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		Assert.assertEquals(21, counter.nextValue());
	}

	@Test
	public void test_nextValue_when_current_thread_mode_when_increment_3() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.CURRENT_THREAD)
			.setIncrement(3)
			.setMaxValue(1000)
			.setStartValue(0)
			.build();
		CommonCounter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		Assert.assertEquals(9, counter.nextValue());
	}

	@Test
	public void test_nextValue_when_all_thread_mode_and_increment_3_and_start_from_10() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.ALL_THREADS)
			.setIncrement(3)
			.setMaxValue(1000)
			.setStartValue(10)
			.build();
		CommonCounter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		Assert.assertEquals(103, counter.nextValue());
	}

	@Test
	public void test_nextValue_when_agent_mode_and_increment_3_and_start_from_10() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.CURRENT_AGENT)
			.setIncrement(3)
			.setMaxValue(1000)
			.setStartValue(10)
			.build();
		CommonCounter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		Assert.assertEquals(55, counter.nextValue());
	}

	@Test
	public void test_nextValue_when_process_mode_and_increment_3_and_start_from_10() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.CURRENT_PROCESS)
			.setIncrement(3)
			.setMaxValue(1000)
			.setStartValue(10)
			.build();
		CommonCounter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		Assert.assertEquals(31, counter.nextValue());
	}

	@Test
	public void test_nextValue_when_current_thread_mode_and_increment_3_and_start_from_10() {
		CounterConfig.Builder counterConfigBuilder = new CounterConfig.Builder();
		CounterConfig counterConfig = counterConfigBuilder
			.setSharingMode(SharingMode.CURRENT_THREAD)
			.setIncrement(3)
			.setMaxValue(1000)
			.setStartValue(10)
			.build();
		CommonCounter counter = new CommonCounter();
		counter.initConfig(counterConfig);
		Assert.assertEquals(19, counter.nextValue());
	}
}
