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

package com.huawei.test.configelement.service.impl;

import com.huawei.test.configelement.service.ExecuteTimesInfo;
import org.junit.Assert;
import org.junit.Test;

public class AllThreadModeCountServiceTest {
	@Test
	public void test_doIncrement_when_condition_valid() {
		ExecuteTimesInfo executeTimesInfo = new ExecuteTimesInfo.Builder()
			.setAgentCount(2)
			.setProcessCount(2)
			.setThreadCount(2)
			.setAgentNumber(1)
			.setProcessNumber(1)
			.setThreadNumber(1)
			.setRunNumber(3) // 第4次执行的取数应该是31
			.build();
		Assert.assertEquals(31, new AllThreadModeCountService().doIncrement(executeTimesInfo));
	}
}
