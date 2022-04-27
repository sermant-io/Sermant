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

package com.huaweicloud.sermant.core.agent.matcher;

@Deprecated
public class ClassMatchers {
    private ClassMatchers() {
    }

    /**
     * 类名字匹配
     *
     * @param name 名字
     * @return NameMatcher
     */
    public static NameMatcher named(String name) {
        return new NameMatcher(name);
    }

    /**
     * 匹配多个类
     *
     * @param classNames 类名数组
     * @return MultiClassMatcher
     */
    public static MultiClassMatcher multiClass(String... classNames) {
        return new MultiClassMatcher(classNames);
    }

    /**
     * 注解匹配
     *
     * @param annotationNames 注解名
     * @return AnnotationMatcher
     */
    public static AnnotationMatcher annotationWith(String... annotationNames) {
        return new AnnotationMatcher(annotationNames);
    }

    /**
     * 注解匹配
     *
     * @param annotations 注解类型
     * @return AnnotationMatcher
     */
    public static AnnotationMatcher annotationWith(Class<?>... annotations) {
        return new AnnotationMatcher(annotations);
    }

    /**
     * 前缀匹配
     *
     * @param prefix 前缀
     * @return PrefixMatcher
     */
    public static PrefixMatcher startWith(String prefix) {
        return new PrefixMatcher(prefix);
    }

    /**
     * 超类匹配
     *
     * @param superTypeNames 超类名
     * @return SuperTypeMatcher
     */
    public static SuperTypeMatcher hasSuperTypes(String... superTypeNames) {
        return new SuperTypeMatcher(superTypeNames);
    }

    /**
     * 超类匹配
     *
     * @param superTypes 超类类型
     * @return SuperTypeMatcher
     */
    public static SuperTypeMatcher hasSuperTypes(Class<?>... superTypes) {
        return new SuperTypeMatcher(superTypes);
    }
}
