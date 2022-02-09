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
 * Based on org/apache/skywalking/apm/agent/core/plugin/AbstractClassEnhancePluginDefine.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.agent.definition;

import com.huawei.sermant.core.agent.matcher.ClassMatcher;

/**
 * 增强定义
 */
@Deprecated
public interface EnhanceDefinition {

    /**
     * 获取待增强的目标类
     *
     * @return 待增强的目标类型匹配器
     */
    ClassMatcher enhanceClass();

    /**
     * 获取封装了待增强目标方法和其拦截器的MethodInterceptPoint
     *
     * @return MethodInterceptPoint数组
     */
    MethodInterceptPoint[] getMethodInterceptPoints();

}
