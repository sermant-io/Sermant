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

/**
 * 功能描述：执行次数配置
 *
 * @author zl
 * @since 2021-12-15
 */
public class ExecuteTimesInfo {
	/**
	 * 当前线程所在agent编号
	 */
	private int agentNumber;

	/**
	 * 当前线程所在process编号
	 */
	private int processNumber;

	/**
	 * 当前线程编号
	 */
	private int threadNumber;

	/**
	 * 当前线程脚本执行次数
	 */
	private int runNumber;

	/**
	 * 执行该脚本的agent总数
	 */
	private int agentCount;

	/**
	 * 一个agent启动的进程总数
	 */
	private int processCount;

	/**
	 * 一个进程中启动的线程总数
	 */
	private int threadCount;

	private ExecuteTimesInfo(Builder builder) {
		this.agentNumber = builder.agentNumber;
		this.processNumber = builder.processNumber;
		this.threadNumber = builder.threadNumber;
		this.runNumber = builder.runNumber;
		this.agentCount = builder.agentCount;
		this.processCount = builder.processCount;
		this.threadCount = builder.threadCount;
	}

	public int getAgentNumber() {
		return agentNumber;
	}

	public int getProcessNumber() {
		return processNumber;
	}

	public int getThreadNumber() {
		return threadNumber;
	}

	public int getRunNumber() {
		return runNumber;
	}

	public int getAgentCount() {
		return agentCount;
	}

	public int getProcessCount() {
		return processCount;
	}

	public int getThreadCount() {
		return threadCount;
	}

	@Override
	public String toString() {
		return "{" +
			"agentNumber=" + agentNumber +
			", processNumber=" + processNumber +
			", threadNumber=" + threadNumber +
			", runNumber=" + runNumber +
			", agentCount=" + agentCount +
			", processCount=" + processCount +
			", threadCount=" + threadCount +
			'}';
	}

	public static class Builder {
		/**
		 * 当前线程所在agent编号
		 */
		private int agentNumber;

		/**
		 * 当前线程所在process编号
		 */
		private int processNumber;

		/**
		 * 当前线程编号
		 */
		private int threadNumber;

		/**
		 * 当前线程脚本执行次数
		 */
		private int runNumber;

		/**
		 * 执行该脚本的agent总数
		 */
		private int agentCount;

		/**
		 * 一个agent启动的进程总数
		 */
		private int processCount;

		/**
		 * 一个进程中启动的线程总数
		 */
		private int threadCount;

		public Builder setAgentNumber(int agentNumber) {
			this.agentNumber = agentNumber;
			return this;
		}

		public Builder setProcessNumber(int processNumber) {
			this.processNumber = processNumber;
			return this;
		}

		public Builder setThreadNumber(int threadNumber) {
			this.threadNumber = threadNumber;
			return this;
		}

		public Builder setRunNumber(int runNumber) {
			this.runNumber = runNumber;
			return this;
		}

		public Builder setAgentCount(int agentCount) {
			this.agentCount = agentCount;
			return this;
		}

		public Builder setProcessCount(int processCount) {
			this.processCount = processCount;
			return this;
		}

		public Builder setThreadCount(int threadCount) {
			this.threadCount = threadCount;
			return this;
		}

		public ExecuteTimesInfo build() {
			return new ExecuteTimesInfo(this);
		}
	}
}
