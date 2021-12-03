/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
