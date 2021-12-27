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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SynchronizingTimerTest {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizingTimerTest.class);

	/**
	 * 统计执行次数
	 */
	private final AtomicInteger sum = new AtomicInteger(0);

	@Test
	public void test_await_when_not_timeout() {
		SynchronizingTimer synchronizingTimer = new SynchronizingTimer(5);
		List<Long> timestamps = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			new Thread(() -> {
				sum.incrementAndGet();
				synchronizingTimer.await(5000, TimeUnit.MILLISECONDS);
				long restartTime = System.currentTimeMillis();
				LOGGER.info("Reset time:{}", restartTime);
				timestamps.add(restartTime);
			}).start();
		}
		sleepThread(2000);
		Assert.assertEquals(3, sum.get());
		for (int i = 1; i < timestamps.size(); i++) {
			Assert.assertEquals(timestamps.get(i - 1), timestamps.get(i));
		}
	}

	@Test
	public void test_await_when_timeout() {
		SynchronizingTimer synchronizingTimer = new SynchronizingTimer(2);
		AtomicInteger counter = new AtomicInteger();
		new Thread(() -> {
			synchronizingTimer.await(3000, TimeUnit.MILLISECONDS);
			counter.getAndIncrement();
		}).start();
		new Thread(() -> {
			sleepThread(5000);
			synchronizingTimer.await(3000, TimeUnit.MILLISECONDS);
			counter.getAndIncrement();
		}).start();
		sleepThread(4000);
		Assert.assertEquals(1, counter.get());
	}

	private void sleepThread(long timeout) {
		try {
			TimeUnit.MILLISECONDS.sleep(timeout);
		} catch (InterruptedException e) {
			Assert.fail();
		}
	}
}
