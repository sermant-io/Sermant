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
 * Based on org/apache/skywalking/apm/plugin/feign/http/v9/define/PathVarInstrumentation.java
 * from the Apache Skywalking project.
 */

package com.huawei.gray.feign.definition;

/**
 * 拦截feign的ApacheHttpClient和OkHttpClient请求
 *
 * @author lilai
 * @since 2021-11-03
 */
public class PathVarDefinition extends AbstractInstDefinition {
    private static final String[] ENHANCE_CLASS = {"feign.ReflectiveFeign$BuildTemplateByResolvingArgs"};

    /**
     * Intercept class.
     */
    private static final String INTERCEPT_CLASS = "com.huawei.gray.feign.interceptor.PathVarInterceptor";

    public PathVarDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, "resolve");
    }
}