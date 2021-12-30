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

package com.huawei.test.scriptexecutor.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 功能描述：用于处理外部进程输入流
 * 外部进程输入流类型
 * 一种是：{@link Process#getInputStream())}
 * 一种是：{@link Process#getErrorStream()}
 *
 * @author zl
 * @since 2021-12-21
 */
public class StreamGobbler extends Thread {
	/**
	 * 日志工具
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(StreamGobbler.class);

	/**
	 * 检查是否完成间隔时间
	 */
	private static final long CHECK_FINISH_INTERVAL = 100L;

	/**
	 * 执行linux命令之后，外部进程输入流
	 */
	private final InputStream is;

	/**
	 * 用于接收linux命令执行输出的缓冲流
	 */
	private final StringBuilder stringBuilder = new StringBuilder();

	/**
	 * 输入流是否处理完成
	 */
	private final AtomicBoolean isFinish = new AtomicBoolean(false);

	/**
	 * 创建该类实例的时候，必须传入线程名称和命令执行外部进程输入流
	 * 外部进程输入流类型
	 * 一种是：{@link Process#getInputStream())}
	 * 一种是：{@link Process#getErrorStream()}
	 *
	 * @param threadName 线程名称
	 * @param is         命令执行外部进程输入流
	 */
	public StreamGobbler(String threadName, InputStream is) {
		super(threadName);
		this.is = is;
	}

	@Override
	public void run() {
		if (is == null) {
			LOGGER.error("Input stream is null.");
			stringBuilder.append("Input stream is null.");
			return;
		}
		try (InputStreamReader isr = new InputStreamReader(is);
			 BufferedReader br = new BufferedReader(isr)) {
			String line;
			while ((line = br.readLine()) != null)
				stringBuilder.append(line);
		} catch (IOException e) {
			LOGGER.error("Handle stream fail.", e);
		} finally {
			isFinish.compareAndSet(false, true);
		}
	}

	/**
	 * 获取InputStream中读入的内容，这里有一个超时判断
	 * <code>
	 * boolean timeout = (waitTimeMillions > 0L) && ((System.currentTimeMillis() - startTime) > waitTimeMillions);
	 * </code>
	 * 传入的waitTimeMillions <= 0L 时，方法会一直等待子线程执行结束再获取内容
	 *
	 * @param waitTimeMillions 等待流中数据读取的时间，如果传入0表示一直等待
	 * @return InputStream中读入的内容
	 */
	public String getExecuteInfo(long waitTimeMillions) throws InterruptedException {
		long startTime = System.currentTimeMillis();
		boolean timeout = false;
		while (!isFinish.get() && !timeout) {
			TimeUnit.MILLISECONDS.sleep(CHECK_FINISH_INTERVAL);

			// 超时判断
			timeout = (waitTimeMillions > 0L) && ((System.currentTimeMillis() - startTime) > waitTimeMillions);
		}
		return stringBuilder.toString();
	}
}
