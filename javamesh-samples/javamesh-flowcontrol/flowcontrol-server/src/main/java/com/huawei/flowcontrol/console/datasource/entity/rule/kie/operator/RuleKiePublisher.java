/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator;

/**
 * kie配置中心降级规则更新、新增、删除类
 *
 * @param <E> 不同规则的entity
 * @author Sherlockhan
 * @since 2020-12-21
 */
public interface RuleKiePublisher<E> {
    void update(String app, E entities) throws Exception;

    void add(String app, E entities);

    void delete(String app, String ruleId);
}
