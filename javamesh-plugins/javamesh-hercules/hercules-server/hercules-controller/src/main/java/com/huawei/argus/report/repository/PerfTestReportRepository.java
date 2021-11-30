package com.huawei.argus.report.repository;

import org.ngrinder.model.PerfTest;
import org.ngrinder.model.PerfTestReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.List;


//@Component
public interface PerfTestReportRepository extends JpaRepository<PerfTestReport, Long> , JpaSpecificationExecutor<PerfTestReport> {


//	@Query("select p.id from PerfTestReport p where PerfTestReport = ?1")
//	@Modifying

	@Query(value = "SELECT * FROM `PERF_TEST_REPORT` WHERE created_user = ?1",nativeQuery = true)
	List<PerfTestReport> getAllByUserId(Long id);

	Page<PerfTestReport> findAll(Specification<PerfTestReport> spec, Pageable pageable);

}
