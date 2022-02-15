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
 * Based on org/apache/servicecomb/governance/marker/operator/ExactOperator.java from the Apache ServiceComb Java
 * Chassis project.
 */

package com.huawei.flowcontrol.common.adapte.cse.match.operator;

import com.huawei.flowcontrol.common.util.StringUtils;

/**
 * 相等匹配
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class ExactOperator implements Operator {
    @Override
    public boolean match(String targetValue, String patternValue) {
        return StringUtils.equal(targetValue, patternValue);
    }

    @Override
    public String getId() {
        return "exact";
    }
}
