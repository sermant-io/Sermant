package com.huawei.argus.testreport.service.listener;

import com.huawei.argus.listener.ITestLifeCycleListener;
import com.huawei.argus.testreport.service.ITestReportService;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.model.TestReport;
import org.ngrinder.perftest.service.PerfTestRunnable;
import org.ngrinder.service.IPerfTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Component
public class TestLifeCycleTestReportListener implements ITestLifeCycleListener {

	public static final String CUSTOM_SCRIPT = "自定义脚本";

	@Autowired
	private ITestReportService testReportService;

	private TestLifeCycleTestReportListener() {
	}

	@Autowired
	ITestLifeCycleListener testLifeCycleTestReportListener;

	@PostConstruct
	public void init() {
		PerfTestRunnable.allTestLifeCycleListeners.add(testLifeCycleTestReportListener);
	}

	@Override
	public void start(PerfTest perfTest, IPerfTestService perfTestService, String version) {

	}

	/**
	 * 保存压测任务正常执行结束和手动停止的数据报告
	 *
	 * @param perfTest        Performance Test
	 * @param stopReason      stop reason
	 * @param perfTestService perfTestService interface
	 * @param version         ngrinder version
	 */
	@Override
	public void finish(PerfTest perfTest, String stopReason, IPerfTestService perfTestService, String version) {
		Long id = perfTest.getId();
		PerfTest test = perfTestService.getOne(id);
		if (test == null) {
			return;
		}
		Status status = test.getStatus();
		Double tps = test.getTps();
		if (tps != null && tps > 0 && (Status.CANCELED.equals(status)) || Status.FINISHED.equals(status)) {
			// 保存自定义脚本数据
			TestReport testReport = new TestReport(test.getCreatedUser());
			testReport.setTestName(test.getTestName());
			testReport.setTestType(CUSTOM_SCRIPT);
			testReport.setStartTime(test.getStartTime());
			testReport.setFinishTime(test.getFinishTime());
			testReport.setRunTime(test.getRuntimeStr());
			testReport.setPerfTestId(id);
			testReportService.save(testReport);
		}
	}
}
