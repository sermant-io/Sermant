package com.huawei.argus.testreport.repository;

import org.ngrinder.model.TestReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TestReportRepository extends JpaRepository<TestReport, Long>, JpaSpecificationExecutor<TestReport> {
}
