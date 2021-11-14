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
			spec = spec.and(idSetEqual("appName", appNames));
		}

		if (!org.springframework.util.StringUtils.isEmpty(createBy)) {
			String[] createBys = createBy.trim().split(",");
			spec = spec.and(idSetEqual("createBy", createBys));
		}

		if (!org.springframework.util.StringUtils.isEmpty(scenarioType)) {
			String[] scenarioTypes = scenarioType.trim().split(",");
			spec = spec.and(idSetEqual("scenarioType", scenarioTypes));
		}

		if (!org.springframework.util.StringUtils.isEmpty(scenarioName)) {
			String[] scenarioTypes = scenarioName.trim().split(",");
			spec = spec.and(idSetEqual("scenarioName", scenarioTypes));
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

	public static Specification<Scenario> idSetEqual(final String column, final Object[] ids) {
		return new Specification<Scenario>() {
			@Override
			public Predicate toPredicate(Root<Scenario> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return root.get(column).in(ids);
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
