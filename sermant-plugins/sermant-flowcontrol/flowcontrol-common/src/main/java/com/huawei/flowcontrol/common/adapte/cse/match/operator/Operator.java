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
 * Based on org/apache/servicecomb/governance/marker/operator/MatchOperator.java
 * from the Apache ServiceComb Java Chassis project.
 */

package com.huawei.flowcontrol.common.adapte.cse.match.operator;

/**
 * 基于{@link com.huawei.flowcontrol.common.adapte.cse.match.RawOperator} 进行匹配
 *
 * @author zhouss
 * @since 2021-11-22
 */
public interface Operator {
    /**
     * 键值匹配
     *
     * @param targetValue 目标匹配串
     * @param patternValue 匹配匹配串
     * @return 是否匹配成功
     */
    boolean match(String targetValue, String patternValue);

    /**
     * 匹配器ID
     * 用于与线上CSE适配， 例如
     * exact  相等
     * prefix 前缀
     * suffix 后缀
     * contain 包含
     *
     * @return id
     */
    String getId();
}
