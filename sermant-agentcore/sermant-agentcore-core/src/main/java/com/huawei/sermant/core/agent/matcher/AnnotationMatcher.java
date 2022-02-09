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
 * Based on org/apache/skywalking/apm/agent/core/plugin/match/ClassAnnotationMatch.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.agent.matcher;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.named;

import com.huawei.sermant.core.utils.Assert;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 注解匹配器
 */
@Deprecated
public class AnnotationMatcher implements NonNameMatcher {

    private final String[] annotationNames;

    public AnnotationMatcher(Class<?>[] annotations) {
        Assert.notEmpty(annotations, "Annotations must not be empty.");
        this.annotationNames = new String[annotations.length];
        for (int i = 0; i < annotations.length; i++) {
            this.annotationNames[i] = annotations[i].getName();
        }
    }

    public AnnotationMatcher(String[] annotationNames) {
        Assert.notEmpty(annotationNames, "Annotation names must not be empty.");
        this.annotationNames = annotationNames;
    }

    @Override
    public ElementMatcher.Junction<TypeDescription> buildJunction() {
        ElementMatcher.Junction<TypeDescription> junction = ElementMatchers.not(isInterface());
        for (String annotationName : annotationNames) {
            junction = junction.and(isAnnotatedWith(named(annotationName)));
        }
        return junction;
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        final AnnotationList annotationsList = typeDescription.getDeclaredAnnotations();
        final HashSet<String> declaredAnnotations = new HashSet<String>();
        for (AnnotationDescription annotationDescription : annotationsList) {
            declaredAnnotations.add(annotationDescription.getAnnotationType().getActualName());
        }
        Set<String> annotations = new HashSet<String>(Arrays.asList(annotationNames));
        annotations.removeAll(declaredAnnotations);
        return annotations.isEmpty();
    }
}
