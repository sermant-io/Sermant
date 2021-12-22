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

package com.huawei.test.timer;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class RandomTimerTest {
	@Test
	public void test_delay_when_start_time_less_than_0() {
		RandomTimer.delay(-1, 3000, TimeUnit.MILLISECONDS);
	}

	@Test
	public void test_delay_when_start_time_equal_0() {
		RandomTimer.delay(0, 3000, TimeUnit.MILLISECONDS);
	}

	@Test
	public void test_delay_when_end_time_less_than_0() {
		RandomTimer.delay(2000, -1, TimeUnit.MILLISECONDS);
	}

	@Test
	public void test_delay_when_end_time_equal_0() {
		RandomTimer.delay(2000, 0, TimeUnit.MILLISECONDS);
	}

	@Test
	public void test_delay_when_end_time_less_than_start_time() {
		RandomTimer.delay(4000, 2000, TimeUnit.MILLISECONDS);
	}

	@Test
	public void test_delay_when_timeUnit_is_null() {
		RandomTimer.delay(2000, 3000, null);
	}

	@Test
	public void test_delay_when_params_are_valid() {
		long startTime = System.currentTimeMillis();
		RandomTimer.delay(2000, 3000, TimeUnit.MILLISECONDS);
		long endTime = System.currentTimeMillis();
		long executeTime = endTime - startTime;
		Assert.assertTrue(executeTime >= 2000 && executeTime <= 3000);
	}
}
