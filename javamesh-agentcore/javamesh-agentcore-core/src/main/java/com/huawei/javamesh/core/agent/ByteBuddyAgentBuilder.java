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
 * Based on org/apache/skywalking/apm/agent/SkyWalkingAgent.java
 * from the Apache Skywalking project.
 */

package com.huawei.javamesh.core.agent;

import java.lang.instrument.Instrumentation;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;

import com.huawei.javamesh.core.agent.transformer.CommonTransformer;

/**
 * 插件增强Builder
 */
public class ByteBuddyAgentBuilder {
    public static void initialize(Instrumentation instrumentation) {
        new AgentBuilder.Default(new ByteBuddy())
            .ignore(new IgnoreClassMatcher())
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .type(EnhanceDefinitionLoader.getInstance().buildMatch())
            .transform(new CommonTransformer())
            .with(new LoadListener())
            .installOn(instrumentation);
    }
}
