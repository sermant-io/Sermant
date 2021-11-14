package com.huawei.argus.listener;

import org.ngrinder.model.PerfTest;
import org.ngrinder.service.IPerfTestService;

public interface ITestLifeCycleListener {
	/**
	 * Callback method which will be invoked whenever {@link PerfTest} is started.
	 *
	 * @param perfTest			Performance Test
	 * @param perfTestService	perfTestService interface
	 * @param version			ngrinder version
	 */
	public void start(PerfTest perfTest, IPerfTestService perfTestService, String version);

	/**
	 * Callback method which will be invoked whenever {@link PerfTest} is finished.
	 *
	 *
	 * @param perfTest			Performance Test
	 * @param stopReason		stop reason
	 * @param perfTestService	perfTestService interface
	 * @param version			ngrinder version
	 */
	public void finish(PerfTest perfTest, String stopReason, IPerfTestService perfTestService, String version);
}
