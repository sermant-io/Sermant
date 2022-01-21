/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.common.adapte.cse.match.operator;

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
     * 所有比较器 map -> name, operator
     */
    private final Map<String, Operator> operators = new HashMap<String, Operator>();

    OperatorManager() {
        loadOperators();
    }

    private void loadOperators() {
        for (Operator operator : ServiceLoader.load(Operator.class, OperatorManager.class.getClassLoader())) {
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
