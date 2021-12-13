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
import com.huawei.test.configelement.impl.CsvParameterized;
import com.huawei.test.configelement.service.ExecuteTimesInfo;
import net.grinder.common.GrinderProperties;
import net.grinder.script.Grinder;
import net.grinder.script.InternalScriptContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ConfigElementTest {
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
	public void test_getExecuteTimesInfo_when_no_runNumber() {
		ConfigElement<ParameterizedConfig> csv = new CsvParameterized();
		ExecuteTimesInfo executeTimesInfo = csv.getExecuteTimesInfo();
		Assert.assertEquals(2, executeTimesInfo.getAgentCount());
		Assert.assertEquals(2, executeTimesInfo.getProcessCount());
		Assert.assertEquals(2, executeTimesInfo.getThreadCount());
		Assert.assertEquals(1, executeTimesInfo.getAgentNumber());
		Assert.assertEquals(1, executeTimesInfo.getProcessNumber());
		Assert.assertEquals(1, executeTimesInfo.getThreadNumber());
		Assert.assertEquals(3, executeTimesInfo.getRunNumber());
	}

	@Test
	public void test_getExecuteTimesInfo_when_has_runNumber() {
		ConfigElement<ParameterizedConfig> csv = new CsvParameterized();
		ExecuteTimesInfo executeTimesInfo = csv.getExecuteTimesInfo(2);
		Assert.assertEquals(2, executeTimesInfo.getAgentCount());
		Assert.assertEquals(2, executeTimesInfo.getProcessCount());
		Assert.assertEquals(2, executeTimesInfo.getThreadCount());
		Assert.assertEquals(1, executeTimesInfo.getAgentNumber());
		Assert.assertEquals(1, executeTimesInfo.getProcessNumber());
		Assert.assertEquals(1, executeTimesInfo.getThreadNumber());
		Assert.assertEquals(2, executeTimesInfo.getRunNumber());
	}
}
