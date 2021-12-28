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

package com.huawei.argus.scene.repository;

import org.ngrinder.model.PerfScene;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * {@link PerfScene} Repository.
 *
 * @Author: j00466872
 * @Date: 2019/4/22 15:09
 */
public interface PerfSceneRepository extends JpaRepository<PerfScene, Long>, JpaSpecificationExecutor<PerfScene> {

	Page<PerfScene> findAll(Specification<PerfScene> spec, Pageable pageable);


	@Query(value = "SELECT * FROM `PERF_SCENE` WHERE created_user = ?1",nativeQuery = true)
	List<PerfScene> getTaskDataByUser(Long id);
}
