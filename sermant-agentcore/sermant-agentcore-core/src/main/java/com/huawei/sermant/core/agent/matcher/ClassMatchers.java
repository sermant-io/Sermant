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
 * Based on org/apache/skywalking/apm/agent/core/plugin/match/NameMatch.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.agent.matcher;

/**
 * 匹配器Facade类
 * <p> Copyright 2021
 *
 * @since 2021
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@Deprecated
public class ClassMatchers {

    public static NameMatcher named(String name) {
        return new NameMatcher(name);
    }

    public static MultiClassMatcher multiClass(String... classNames) {
        return new MultiClassMatcher(classNames);
    }

    public static AnnotationMatcher annotationWith(String... annotationNames) {
        return new AnnotationMatcher(annotationNames);
    }

    public static AnnotationMatcher annotationWith(Class<?>... annotations) {
        return new AnnotationMatcher(annotations);
    }

    public static PrefixMatcher startWith(String prefix) {
        return new PrefixMatcher(prefix);
    }

    public static SuperTypeMatcher hasSuperTypes(String... superTypeNames) {
        return new SuperTypeMatcher(superTypeNames);
    }

    public static SuperTypeMatcher hasSuperTypes(Class<?>... superTypes) {
        return new SuperTypeMatcher(superTypes);
    }
}
