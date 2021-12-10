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

import com.huawei.argus.scenario.repository.ScenarioRepository;
import com.huawei.argus.scenario.service.IScenarioService;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.model.Role;
import org.ngrinder.model.Scenario;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;

@Service
public class ScenarioService implements IScenarioService {
	@Autowired
	ScenarioRepository scenarioRepository;

	@Override
	public List<Scenario> findAll() {
		return scenarioRepository.findAll();
	}

	@Override
	public Scenario save(Scenario scenario) {
		return scenarioRepository.saveAndFlush(scenario);
	}

	@Override
	public Page<Scenario> getPagedAll(User user, String query, String appName, String createBy, String scenarioType, String scenarioName, Pageable pageable) {
		Specifications<Scenario> spec = Specifications.where(idEmptyPredicate());
		// User can see only his own test
		if (user.getRole().equals(Role.USER)) {
			spec = spec.and(createdBy(user));
		}

		if (!org.springframework.util.StringUtils.isEmpty(appName)) {
			String[] appNames = appName.trim().split(",");
			spec = spec.and(setEqual("appName", appNames));
		}

		if (!org.springframework.util.StringUtils.isEmpty(createBy)) {
			String[] createBys = createBy.trim().split(",");
			spec = spec.and(setEqual("createBy", createBys));
		}

		if (!org.springframework.util.StringUtils.isEmpty(scenarioType)) {
			String[] scenarioTypes = scenarioType.trim().split(",");
			spec = spec.and(setEqual("scenarioType", scenarioTypes));
		}

		if (!org.springframework.util.StringUtils.isEmpty(scenarioName)) {
			String[] scenarioTypes = scenarioName.trim().split(",");
			spec = spec.and(setEqual("scenarioName", scenarioTypes));
		}

		if (StringUtils.isNotBlank(query)) {
			spec = spec.and(likeColumns(query));
		}
		return scenarioRepository.findAll(spec, pageable);
	}

	@Override
	public Scenario getOne(Long testId) {
		return scenarioRepository.findOne(testId);
	}

	@Transactional
	public void delete(User user, Long id) {
		Scenario scenario = getOne(id);
		scenarioRepository.delete(scenario);
	}

	@Override
	public List<Scenario> getAll(Long[] ids) {
		if (ids.length == 0) {
			return newArrayList();
		}
		Specifications<Scenario> spec = Specifications.where(idEmptyPredicate());
		spec = spec.and(setEqual("id", ids));
		return scenarioRepository.findAll(spec);
	}

	@Override
	public List<Scenario> getAllByScriptPaths(User user, List<String> scriptPaths) {
		if (scriptPaths.size() == 0) {
			return newArrayList();
		}
		Specifications<Scenario> spec = Specifications.where(idEmptyPredicate());
		spec = spec.and(setEqual("scriptPath", scriptPaths.toArray()));
		return scenarioRepository.findAll(spec);
	}

	public static Specification<Scenario> setEqual(final String column, final Object[] values) {
		return new Specification<Scenario>() {
			@Override
			public Predicate toPredicate(Root<Scenario> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get(column).in(values);
			}
		};
	}

	private static Specification<Scenario> likeColumns(final String queryString) {
		return new Specification<Scenario>() {
			@Override
			public Predicate toPredicate(Root<Scenario> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				String queryStr = ("%" + queryString + "%").toLowerCase();
				return cb.or(cb.like(cb.lower(root.get("label").as(String.class)), queryStr),
					cb.like(root.get("appName").as(String.class), queryStr),
					cb.like(root.get("scenarioName").as(String.class), queryStr),
					cb.like(root.get("desc").as(String.class), queryStr));
			}
		};
	}

	private static Specification<Scenario> idEmptyPredicate() {
		return new Specification<Scenario>() {
			@Override
			public Predicate toPredicate(Root<Scenario> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("id").isNotNull();
			}
		};
	}

	private static Specification<Scenario> createdBy(final User user) {
		return new Specification<Scenario>() {
			@Override
			public Predicate toPredicate(Root<Scenario> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.or(cb.equal(root.get("createBy"), user));
			}
		};
	}
}
