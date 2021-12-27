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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 功能描述：线程集合点函数实现
 *
 * @author zl
 * @since 2021-12-21
 */
public class SynchronizingTimer {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizingTimer.class);

	/**
	 * 线程循环栅栏同步器
	 */
	private final CyclicBarrier cyclicBarrier;

	/**
	 * 初始化时指定同步线程数量和最后一个到达集合点的线程需要完成的任务
	 *
	 * @param threadCount 需要同步的线程数量
	 * @param lastTask    最后一个到达的线程需要完成的任务
	 */
	public SynchronizingTimer(int threadCount, Runnable lastTask) {
		this.cyclicBarrier = new CyclicBarrier(threadCount, lastTask);
	}

	/**
	 * 只指定需要同步的线程数量
	 *
	 * @param threadCount 需要同步的线程数量
	 */
	public SynchronizingTimer(int threadCount) {
		this.cyclicBarrier = new CyclicBarrier(threadCount);
	}

	/**
	 * 执行线程等待操作，等满足要求的线程都到达这个拦截器时，才继续向下面执行
	 *
	 * @param timeout  超时时间，及时没达到要求的线程数，到达这个时间时就抛异常
	 * @param timeUnit 超时时间单位
	 * @return 当前线程调用这个方法的顺序，即第几个调用这个方法，-1表示抛异常了
	 */
	public int await(long timeout, TimeUnit timeUnit) {
		try {
			return cyclicBarrier.await(timeout, timeUnit);
		} catch (InterruptedException e) {
			LOGGER.error("Thread Interrupted.");
		} catch (BrokenBarrierException e) {
			LOGGER.error("Thread barrier were broken.");
		} catch (TimeoutException e) {
			LOGGER.error("Thread barrier timeout.");
		}
		return -1;
	}

	/**
	 * 如果栅栏已经损坏，通过这个可以重置栅栏，注意使用{@link com.huawei.test.timer.SynchronizingTimer#isBroken()}判断
	 */
	public void reset() {
		cyclicBarrier.reset();
	}

	/**
	 * 返回跳过此栅栏所需的线程数.
	 *
	 * @return 跳过此栅栏所需的线程数.
	 */
	public int getParties() {
		return cyclicBarrier.getParties();
	}

	/**
	 * 已经到达这个栅栏的线程数.
	 *
	 * @return 已经到达这个栅栏的线程数
	 */
	public int getNumberWaiting() {
		return cyclicBarrier.getNumberWaiting();
	}

	/**
	 * 查询栅栏是否是破坏状态，被中断或者超时都会造成栅栏破坏
	 *
	 * @return true：破坏，false：未破坏.
	 */
	public boolean isBroken() {
		return cyclicBarrier.isBroken();
	}
}
