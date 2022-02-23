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
 */

/**
 * Based on org/apache/servicecomb/governance/marker/operator/CompareOperator.java from the Apache ServiceComb Java
 * Chassis project.
 */

package com.huawei.flowcontrol.common.adapte.cse.match.operator;

import com.huawei.flowcontrol.common.util.StringUtils;
import com.huawei.sermant.core.common.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 比较, 格式如下, 存在有两个符合和一个符号的场景 请求路径包含/b 且    d=10 e != 5 f >= 11 - apiPath: contains: /b headers: d: compare: =10 e:
 * compare: '!=5' f: compare: '>=11' method: - PUT name: rule2
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class CompareOperator implements Operator {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final double DOUBLE_EPSLON = 1e-6d;

    private final Set<Character> operators = new HashSet<Character>();

    public CompareOperator() {
        operators.addAll(Arrays.asList('=', '>', '<', '!'));
    }

    @Override
    public boolean match(String targetValue, String patternValue) {
        if (targetValue == null || patternValue == null) {
            return false;
        }
        if (operators.contains(patternValue.charAt(0)) && operators.contains(patternValue.charAt(1))) {
            // 前两个字符为比较符
            return compare(targetValue, patternValue.substring(0, 2), patternValue.substring(2));
        } else if (operators.contains(patternValue.charAt(0))) {
            // 仅第一个为比较字符
            return compare(targetValue, patternValue.substring(0, 1), patternValue.substring(1));
        } else {
            return false;
        }
    }

    private boolean compare(String targetValue, String operator, String num) {
        double parsedTarget;
        double parsedNum;
        try {
            parsedTarget = Double.parseDouble(targetValue);
            parsedNum = Double.parseDouble(num);
        } catch (NumberFormatException ex) {
            // 转换失败的直接返回未匹配
            LOGGER.warning(
                String.format(Locale.ENGLISH, "Format number failed when convert %s and %s", targetValue, num));
            return false;
        }

        // 根据operator进行比较
        if (StringUtils.equal(operator, "=")) {
            return doubleEquals(parsedTarget, parsedNum);
        } else if (StringUtils.equal(operator, ">")) {
            return parsedTarget > parsedNum;
        } else if (StringUtils.equal(operator, "<")) {
            return parsedTarget < parsedNum;
        } else if (StringUtils.equal(operator, "!=") || StringUtils.equal(operator, "!")) {
            return !doubleEquals(parsedTarget, parsedNum);
        } else if (StringUtils.equal(operator, "<=")) {
            return parsedTarget <= parsedNum;
        } else if (StringUtils.equal(operator, ">=")) {
            return parsedTarget >= parsedNum;
        } else {
            LOGGER.warning(String.format(Locale.ENGLISH, "Not support operator %s", operator));
            return false;
        }
    }

    @Override
    public String getId() {
        return "compare";
    }

    private boolean doubleEquals(double target, double result) {
        return Math.abs(target - result) < DOUBLE_EPSLON;
    }
}
