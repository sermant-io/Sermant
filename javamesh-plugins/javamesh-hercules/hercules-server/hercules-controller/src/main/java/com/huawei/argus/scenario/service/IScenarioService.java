
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

package com.huawei.argus.scenario.service;

import org.ngrinder.model.Scenario;
import org.ngrinder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IScenarioService {
	List<Scenario> findAll();

	/**
	 * 保存压测场景
	 * @param scenario
	 * @return
	 */
	Scenario save(Scenario scenario);

	/**
	 * 压测场景查询
	 * @param user
	 * @param query
	 * @param appNames
	 * @param createBy
	 * @param scenarioType
	 * @return
	 */
	Page<Scenario> getPagedAll(User user, String query, String appNames, String createBy, String scenarioType, String scenarioName, Pageable pageable);

	/**
	 * 查询
	 * @param id
	 * @return
	 */
	Scenario getOne(Long id);

	/**
	 * 删除
	 * @param user
	 * @param id
	 */
	void delete(User user, Long id);

	/**
	 * 根据ID批量查询
	 * @param ids
	 * @return
	 */
	List<Scenario> getAll(Long[] ids);


	/**
	 * 根据脚本查询场景
	 * @param user
	 * @param scriptPaths
	 * @return
	 */
	List<Scenario> getAllByScriptPaths(User user, List<String> scriptPaths);
}
