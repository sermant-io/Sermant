/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator;

/**
 * kie配置中心规则查询接口层
 *
 * @param <E> 不同规则的entity
 * @author Sherlockhan
 * @since 2020-12-21
 */
public interface RuleKieProvider<E> {
    /**
     * 配置标签的最大长度
     */
    int MAX_LENGTH_OF_KIE_LABEL = 31;

    E getRules(String app) throws Exception;
}
