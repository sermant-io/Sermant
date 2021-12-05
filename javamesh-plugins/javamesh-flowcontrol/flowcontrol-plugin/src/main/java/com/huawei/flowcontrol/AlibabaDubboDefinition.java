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
 *
 * Based on DubboInstrumentation.java from the Apache Skywalking project.
 */

package com.huawei.flowcontrol;

/**
 * 拦截点
 * 拦截 alibaba MonitorFilter invoke
 *
 * @author liyi
 * @since 2020-08-26
 */
public class AlibabaDubboDefinition extends DubboDefinition {
    /**
     * 增强类的全限定名
     */
    private static final String ENHANCE_CLASS = "com.alibaba.dubbo.monitor.support.MonitorFilter";

    /**
     * 拦截类的全限定名
     */
    private static final String INTERCEPT_CLASS = "com.huawei.flowcontrol.AlibabaDubboInterceptor";

    public AlibabaDubboDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS);
    }
}
