package com.huawei.argus.testreport.service;

import org.ngrinder.model.TestReport;
import org.ngrinder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

public interface ITestReportService {
	TestReport getOne(Long id);

	TestReport save(TestReport scenario);

	Page<TestReport> getPagedAll(User user, String query, String testType, String testName,
								 String startTime, String endTime, Pageable pageable);

	void delete(User user, long id);
}
