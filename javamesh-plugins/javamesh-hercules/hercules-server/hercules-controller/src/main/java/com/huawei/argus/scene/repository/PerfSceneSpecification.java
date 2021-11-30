package com.huawei.argus.scene.repository;

import org.ngrinder.model.PerfScene;
import org.ngrinder.model.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @Author: j00466872
 * @Date: 2019/4/23 16:50
 */
public abstract class PerfSceneSpecification {

	/**
	 * 获取createBy规则：用户user创建的场景
	 * @param user
	 * @return
	 */
	public static Specification<PerfScene> createdBy(final User user) {
		return new Specification<PerfScene>() {
			@Override
			public Predicate toPredicate(Root<PerfScene> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.or(criteriaBuilder.equal(root.get("createdUser"), user));
			}
		};
	}
}
