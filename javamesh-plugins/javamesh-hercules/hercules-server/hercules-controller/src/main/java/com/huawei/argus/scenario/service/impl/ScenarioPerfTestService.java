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

package com.huawei.argus.scenario.service.impl;

import com.huawei.argus.scenario.repository.ScenarioPerfTestRepository;
import com.huawei.argus.scenario.service.IScenarioPerfTestService;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.ScenarioPerfTest;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Service
public class ScenarioPerfTestService implements IScenarioPerfTestService {

	@Autowired
	ScenarioPerfTestRepository scenarioPerfTestRepository;

	@Override
	public ScenarioPerfTest save(ScenarioPerfTest scenarioPerfTest) {
		return scenarioPerfTestRepository.save(scenarioPerfTest);
	}

	@Override
	public List<ScenarioPerfTest> getAllByID(User user, Long perfTestId, Long scenarioId) {
		Specifications<ScenarioPerfTest> spec = Specifications.where(idEmptyPredicate());

		if (!org.springframework.util.StringUtils.isEmpty(scenarioId)) {
			spec = spec.and(idSetEqual("scenarioId", scenarioId));
		}

		if (!org.springframework.util.StringUtils.isEmpty(perfTestId)) {
			spec = spec.and(idSetEqual("perfTestId", perfTestId));
		}

		return scenarioPerfTestRepository.findAll(spec);
	}

	@Override
	public List<ScenarioPerfTest> getAllByScenarioIds(User user, Long[] scenarioIds) {
		Specifications<ScenarioPerfTest> spec = Specifications.where(idEmptyPredicate());
		if (!org.springframework.util.StringUtils.isEmpty(scenarioIds)) {
			spec = spec.and(idSetEqual("scenarioId", scenarioIds));
		}
		return scenarioPerfTestRepository.findAll(spec);
	}

	@Override
	public void deleteByOneId(User user, Long id, Long perfTestId, Long scenarioId) {
		scenarioPerfTestRepository.deleteByOneId(id, perfTestId, scenarioId);
	}


	private static Specification<ScenarioPerfTest> idEmptyPredicate() {
		return new Specification<ScenarioPerfTest>() {
			@Override
			public Predicate toPredicate(Root<ScenarioPerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("id").isNotNull();
			}
		};
	}

	public static Specification<ScenarioPerfTest> idSetEqual(final String column, final Long id) {
		return new Specification<ScenarioPerfTest>() {
			@Override
			public Predicate toPredicate(Root<ScenarioPerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get(column).in(id);
			}
		};
	}

	public static Specification<ScenarioPerfTest> idSetEqual(final String column, final Long[] ids) {
		return new Specification<ScenarioPerfTest>() {
			@Override
			public Predicate toPredicate(Root<ScenarioPerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get(column).in((Object[]) ids);
			}
		};
	}
}
