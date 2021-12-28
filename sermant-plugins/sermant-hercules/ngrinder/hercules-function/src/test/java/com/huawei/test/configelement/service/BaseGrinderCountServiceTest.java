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

package com.huawei.test.configelement.service;

import com.huawei.test.configelement.service.impl.AgentModeCountService;
import com.huawei.test.configelement.service.impl.AllThreadModeCountService;
import com.huawei.test.configelement.service.impl.CurrentThreadModeCountService;
import com.huawei.test.configelement.service.impl.ProcessModeCountService;
import org.junit.Assert;
import org.junit.Test;

public class BaseGrinderCountServiceTest {

	@Test
	public void test_nextIncrementNumber_when_all_thread_mode() {
		ExecuteTimesInfo executeTimesInfo = new ExecuteTimesInfo.Builder()
			.setAgentCount(2)
			.setProcessCount(2)
			.setThreadCount(2)
			.setAgentNumber(1)
			.setProcessNumber(1)
			.setThreadNumber(1)
			.setRunNumber(3) // 第4次执行的取数应该是31
			.build();
		Assert.assertEquals(31, new AllThreadModeCountService().nextIncrementNumber(executeTimesInfo));
	}

	@Test
	public void test_nextIncrementNumber_when_agent_mode() {
		ExecuteTimesInfo executeTimesInfo = new ExecuteTimesInfo.Builder()
			.setAgentCount(2)
			.setProcessCount(2)
			.setThreadCount(2)
			.setAgentNumber(1)
			.setProcessNumber(1)
			.setThreadNumber(1)
			.setRunNumber(3) // 第4次执行的取数应该是15
			.build();
		Assert.assertEquals(15, new AgentModeCountService().nextIncrementNumber(executeTimesInfo));
	}

	@Test
	public void test_nextIncrementNumber_when_process_mode() {
		ExecuteTimesInfo executeTimesInfo = new ExecuteTimesInfo.Builder()
			.setAgentCount(2)
			.setProcessCount(2)
			.setThreadCount(2)
			.setAgentNumber(1)
			.setProcessNumber(1)
			.setThreadNumber(1)
			.setRunNumber(3) // 第4次执行的取数应该是7
			.build();
		Assert.assertEquals(7, new ProcessModeCountService().nextIncrementNumber(executeTimesInfo));
	}

	@Test
	public void test_nextIncrementNumber_when_current_thread_mode() {
		ExecuteTimesInfo executeTimesInfo = new ExecuteTimesInfo.Builder()
			.setAgentCount(2)
			.setProcessCount(2)
			.setThreadCount(2)
			.setAgentNumber(1)
			.setProcessNumber(1)
			.setThreadNumber(1)
			.setRunNumber(3) // 第4次执行的取数应该是3
			.build();
		Assert.assertEquals(3, new CurrentThreadModeCountService().nextIncrementNumber(executeTimesInfo));
	}


	@Test
	public void test_isValid_when_agent_number_is_invalid() {
		ExecuteTimesInfo executeTimesInfo = new ExecuteTimesInfo.Builder()
			.setAgentCount(2)
			.setProcessCount(2)
			.setThreadCount(2)
			.setAgentNumber(-1)
			.setProcessNumber(1)
			.setThreadNumber(1)
			.setRunNumber(3) // 第4次执行的取数应该是3
			.build();
		boolean valid = new AgentModeCountService().isValid(executeTimesInfo);
		Assert.assertFalse(valid);
	}

	@Test
	public void test_isValid_when_process_number_is_invalid() {
		ExecuteTimesInfo executeTimesInfo = new ExecuteTimesInfo.Builder()
			.setAgentCount(2)
			.setProcessCount(2)
			.setThreadCount(2)
			.setAgentNumber(1)
			.setProcessNumber(-1)
			.setThreadNumber(1)
			.setRunNumber(3) // 第4次执行的取数应该是3
			.build();
		boolean valid = new AgentModeCountService().isValid(executeTimesInfo);
		Assert.assertFalse(valid);
	}

	@Test
	public void test_isValid_when_thread_number_is_invalid() {
		ExecuteTimesInfo executeTimesInfo = new ExecuteTimesInfo.Builder()
			.setAgentCount(2)
			.setProcessCount(2)
			.setThreadCount(2)
			.setAgentNumber(1)
			.setProcessNumber(1)
			.setThreadNumber(-1)
			.setRunNumber(3) // 第4次执行的取数应该是3
			.build();
		boolean valid = new AgentModeCountService().isValid(executeTimesInfo);
		Assert.assertFalse(valid);
	}

	@Test
	public void test_isValid_when_run_number_is_invalid() {
		ExecuteTimesInfo executeTimesInfo = new ExecuteTimesInfo.Builder()
			.setAgentCount(2)
			.setProcessCount(2)
			.setThreadCount(2)
			.setAgentNumber(1)
			.setProcessNumber(1)
			.setThreadNumber(1)
			.setRunNumber(-1) // 第4次执行的取数应该是3
			.build();
		boolean valid = new AgentModeCountService().isValid(executeTimesInfo);
		Assert.assertFalse(valid);
	}

	@Test
	public void test_isValid_when_agent_count_is_invalid() {
		ExecuteTimesInfo executeTimesInfo = new ExecuteTimesInfo.Builder()
			.setAgentCount(0)
			.setProcessCount(2)
			.setThreadCount(2)
			.setAgentNumber(1)
			.setProcessNumber(1)
			.setThreadNumber(1)
			.setRunNumber(3) // 第4次执行的取数应该是3
			.build();
		boolean valid = new AgentModeCountService().isValid(executeTimesInfo);
		Assert.assertFalse(valid);
	}

	@Test
	public void test_isValid_when_process_count_is_invalid() {
		ExecuteTimesInfo executeTimesInfo = new ExecuteTimesInfo.Builder()
			.setAgentCount(2)
			.setProcessCount(0)
			.setThreadCount(2)
			.setAgentNumber(1)
			.setProcessNumber(1)
			.setThreadNumber(1)
			.setRunNumber(3) // 第4次执行的取数应该是3
			.build();
		boolean valid = new AgentModeCountService().isValid(executeTimesInfo);
		Assert.assertFalse(valid);
	}

	@Test
	public void test_isValid_when_thread_count_is_invalid() {
		ExecuteTimesInfo executeTimesInfo = new ExecuteTimesInfo.Builder()
			.setAgentCount(2)
			.setProcessCount(2)
			.setThreadCount(0)
			.setAgentNumber(1)
			.setProcessNumber(1)
			.setThreadNumber(1)
			.setRunNumber(3) // 第4次执行的取数应该是3
			.build();
		boolean valid = new AgentModeCountService().isValid(executeTimesInfo);
		Assert.assertFalse(valid);
	}
}
