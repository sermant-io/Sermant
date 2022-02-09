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
 * Based on org/apache/skywalking/apm/agent/core/plugin/match/MultiClassNameMatch.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.agent.matcher;

import com.huawei.sermant.core.utils.Assert;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 多个类匹配
 */
@Deprecated
public class MultiClassMatcher implements NonNameMatcher {

    private final Set<String> classNames;

    MultiClassMatcher(String[] classNames) {
        Assert.notEmpty(classNames, "Class names must not be empty.");
        this.classNames = new HashSet<String>(Arrays.asList(classNames));
    }

    @Override
    public ElementMatcher.Junction<TypeDescription> buildJunction() {
        return new ElementMatcher.Junction.AbstractBase<TypeDescription>() {
            @Override
            public boolean matches(TypeDescription target) {
                return classNames.contains(target.getActualName());
            }
        };
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        return classNames.contains(typeDescription.getActualName());
    }
}
