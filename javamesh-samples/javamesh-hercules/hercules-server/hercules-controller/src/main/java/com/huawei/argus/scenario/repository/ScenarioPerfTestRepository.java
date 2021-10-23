package com.huawei.argus.scenario.repository;

import org.ngrinder.model.ScenarioPerfTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ScenarioPerfTestRepository extends JpaRepository<ScenarioPerfTest, Long>, JpaSpecificationExecutor<ScenarioPerfTest> {

	/**
	 * 三个ID，只有一个有值时使用此接口
	 * @param id
	 * @param perfTestId
	 * @param scenarioId
	 */
	@Transactional
	@Modifying
	@Query(value = "delete from SCENARIO_PERF_TEST where id =?1 or perf_test_id =?2 or scenario_id =?3",nativeQuery = true)
	void deleteByOneId(Long id, Long perfTestId, Long scenarioId);
}
