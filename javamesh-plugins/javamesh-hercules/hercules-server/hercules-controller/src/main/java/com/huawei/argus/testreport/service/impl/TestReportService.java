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

package com.huawei.argus.testreport.service.impl;

import com.huawei.argus.testreport.repository.TestReportRepository;
import com.huawei.argus.testreport.service.ITestReportService;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Role;
import org.ngrinder.model.TestReport;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.util.Date;

@Service
public class TestReportService implements ITestReportService {

	@Autowired
	private TestReportRepository testReportRepository;

	@Override
	public TestReport getOne(Long id) {
		return testReportRepository.findOne(id);
	}

	@Override
	public TestReport save(TestReport scenario) {
		return testReportRepository.saveAndFlush(scenario);
	}

	@Override
	public Page<TestReport> getPagedAll(User user, String query, String testType, String testName,
										String startTime, String endTime, Pageable pageable) {
		Specifications<TestReport> spec = Specifications.where(idEmptyPredicate());

		if (user.getRole().equals(Role.USER)) {
			spec = spec.and(createdBy(user));
		}

		if (!org.springframework.util.StringUtils.isEmpty(testType)) {
			String[] testTypes = testType.trim().split(",");
			spec = spec.and(setEqual("testType", testTypes));
		}

		if (!org.springframework.util.StringUtils.isEmpty(testName)) {
			String[] testNames = testName.trim().split(",");
			spec = spec.and(setEqual("testName", testNames));
		}

		if (!org.springframework.util.StringUtils.isEmpty(startTime)) {
			try {
				Date startDate = DateUtils.toDate(startTime);
				spec = spec.and(greaterThanOrEqualTo(startDate));
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}

		if (!org.springframework.util.StringUtils.isEmpty(endTime)) {
			try {
				Date endDate = DateUtils.toDate(endTime);
				spec = spec.and(lessThanOrEqualTo(endDate));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		if (StringUtils.isNotBlank(query)) {
			spec = spec.and(likeColumns(query));
		}
		return testReportRepository.findAll(spec, pageable);
	}

	@Override
	public void delete(User user, long id) {
		TestReport testReport = getOne(id);
		testReportRepository.delete(testReport);
	}

	public static Specification<TestReport> greaterThanOrEqualTo(final Date startTime) {
		return new Specification<TestReport>() {
			@Override
			public Predicate toPredicate(Root<TestReport> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.greaterThanOrEqualTo(root.<Date>get("startTime"), startTime);
			}
		};
	}

	public static Specification<TestReport> lessThanOrEqualTo(final Date endTime) {
		return new Specification<TestReport>() {
			@Override
			public Predicate toPredicate(Root<TestReport> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.lessThanOrEqualTo(root.<Date>get("finishTime"), endTime);
			}
		};
	}


	public static Specification<TestReport> setEqual(final String column, final Object[] ids) {
		return new Specification<TestReport>() {
			@Override
			public Predicate toPredicate(Root<TestReport> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get(column).in(ids);
			}
		};
	}

	private static Specification<TestReport> likeColumns(final String queryString) {
		return new Specification<TestReport>() {
			@Override
			public Predicate toPredicate(Root<TestReport> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				String queryStr = ("%" + queryString + "%").toLowerCase();
				return cb.or(cb.like(cb.lower(root.get("testName").as(String.class)), queryStr));
			}
		};
	}

	private static Specification<TestReport> idEmptyPredicate() {
		return new Specification<TestReport>() {
			@Override
			public Predicate toPredicate(Root<TestReport> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get("id").isNotNull();
			}
		};
	}

	private static Specification<TestReport> createdBy(final User user) {
		return new Specification<TestReport>() {
			@Override
			public Predicate toPredicate(Root<TestReport> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.or(cb.equal(root.get("createdUser"), user));
			}
		};
	}
}
