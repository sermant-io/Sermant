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

/*
 * Based on org/apache/skywalking/apm/agent/core/plugin/match/IndirectMatch.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.agent.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 非类名匹配器接口
 */
@Deprecated
public interface NonNameMatcher extends ClassMatcher {

    /**
     * 构造类型匹配条件连接点
     *
     * @return 匹配条件连接点
     */
    ElementMatcher.Junction<TypeDescription> buildJunction();

    /**
     * 判断目标类型是否匹配
     *
     * @param typeDescription 目标类型
     * @return 如果匹配返回true，否则false
     */
    boolean isMatch(TypeDescription typeDescription);
}
