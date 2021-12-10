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
