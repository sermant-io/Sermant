/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.match.operator;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 基于SPI将所有operator加载进来
 *
 * @author zhouss
 * @since 2021-11-24
 */
public enum OperatorManager {
    /**
     * 单例
     */
    INSTANCE;

    /**
     * 所有比较器
     * map<name, operator>
     */
    private final Map<String, Operator> operators = new HashMap<String, Operator>();

    OperatorManager() {
        loadOperators();
    }

    private void loadOperators() {
        for(Operator operator : ServiceLoader.load(Operator.class)) {
            operators.put(operator.getId(), operator);
        }
    }

    /**
     * 获取比较器
     *
     * @param id 比较器ID
     * @return Operator
     */
    public Operator getOperator(String id) {
        return operators.get(id);
    }
}
