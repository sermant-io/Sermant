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
 * Based on org/apache/skywalking/apm/agent/core/plugin/match/HierarchyMatch.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.agent.matcher;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.named;

import com.huawei.sermant.core.utils.Assert;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 父类匹配器
 */
@Deprecated
public class SuperTypeMatcher implements NonNameMatcher {

    private final String[] superTypes;

    public SuperTypeMatcher(Class<?>[] superTypes) {
        Assert.notEmpty(superTypes, "Super types must not be empty.");
        this.superTypes = new String[superTypes.length];
        for (int i = 0; i < superTypes.length; i++) {
            this.superTypes[i] = superTypes[i].getName();
        }
    }

    public SuperTypeMatcher(String[] superTypeNames) {
        Assert.notEmpty(superTypeNames, "Super types must not be empty.");
        this.superTypes = superTypeNames;
    }

    @Override
    public ElementMatcher.Junction<TypeDescription> buildJunction() {
        ElementMatcher.Junction<TypeDescription> junction = ElementMatchers.not(isInterface());
        for (String superType : superTypes) {
            junction = junction.and(hasSuperType(named(superType)));
        }
        return junction;
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        final HashSet<String> types = new HashSet<String>(Arrays.asList(superTypes));
        Queue<TypeDescription.Generic> queue = new LinkedList<TypeDescription.Generic>();
        queue.add(typeDescription.asGenericType());

        for (TypeDescription.Generic current = queue.poll();
             current != null && !types.isEmpty();
             current = queue.poll()) {
            types.remove(current.asRawType().getActualName());
            TypeList.Generic interfaces = current.getInterfaces();
            if (!interfaces.isEmpty()) {
                queue.addAll(interfaces);
            }
            TypeDescription.Generic superClass = current.getSuperClass();
            if (superClass != null) {
                queue.add(superClass);
            }
        }
        return types.isEmpty();
    }
}
