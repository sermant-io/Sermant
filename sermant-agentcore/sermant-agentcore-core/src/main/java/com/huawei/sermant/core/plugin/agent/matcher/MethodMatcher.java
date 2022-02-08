/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.core.plugin.agent.matcher;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 方法匹配器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public abstract class MethodMatcher implements ElementMatcher<MethodDescription> {
    /**
     * 匹配任意方法
     *
     * @return 方法匹配器对象
     */
    public static MethodMatcher any() {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return true;
            }
        };
    }

    /**
     * 匹配名称完全一致的方法
     *
     * @param methodName 方法名称
     * @return 方法匹配器对象
     */
    public static MethodMatcher nameEquals(String methodName) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return methodDescription.getActualName().equals(methodName);
            }
        };
    }

    /**
     * 匹配多个方法
     *
     * @param methodNames 方法集
     * @return 方法匹配器对象
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public static MethodMatcher nameContains(String... methodNames) {
        return nameContains(new HashSet<>(Arrays.asList(methodNames)));
    }

    /**
     * 匹配多个方法
     *
     * @param methodNames 方法集
     * @return 方法匹配器对象
     */
    public static MethodMatcher nameContains(Set<String> methodNames) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return methodNames.contains(methodDescription.getActualName());
            }
        };
    }

    /**
     * 匹配方法名前缀满足要求的方法
     *
     * @param prefix 前缀
     * @return 方法匹配器对象
     */
    public static MethodMatcher namePrefixedWith(String prefix) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return methodDescription.getActualName().startsWith(prefix);
            }
        };
    }

    /**
     * 匹配方法名后缀满足要求的方法
     *
     * @param suffix 后缀
     * @return 方法匹配器对象
     */
    public static MethodMatcher nameSuffixedWith(String suffix) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return methodDescription.getActualName().endsWith(suffix);
            }
        };
    }

    /**
     * 匹配方法名内缀满足要求的方法
     *
     * @param infix 内缀
     * @return 方法匹配器对象
     */
    public static MethodMatcher nameInfixedWith(String infix) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return methodDescription.getActualName().contains(infix);
            }
        };
    }

    /**
     * 匹配满足正则表达式的方法
     *
     * @param pattern 正则表达式
     * @return 方法匹配器对象
     */
    public static MethodMatcher nameMatches(String pattern) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return methodDescription.getActualName().matches(pattern);
            }
        };
    }

    /**
     * 匹配被传入所有注解修饰的方法
     *
     * @param annotations 注解集
     * @return 方法匹配器对象
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public static MethodMatcher isAnnotatedWith(String... annotations) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                final Set<String> annotationSet = new HashSet<String>(Arrays.asList(annotations));
                final AnnotationList annotationList = methodDescription.getDeclaredAnnotations();
                for (AnnotationDescription description : annotationList) {
                    annotationSet.remove(description.getAnnotationType().getActualName());
                }
                return annotationSet.isEmpty();
            }
        };
    }

    /**
     * 匹配被传入所有注解修饰的方法
     *
     * @param annotations 注解集
     * @return 方法匹配器对象
     */
    @SafeVarargs
    public static MethodMatcher isAnnotatedWith(Class<? extends Annotation>... annotations) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                final AnnotationList annotationList = methodDescription.getDeclaredAnnotations();
                for (Class<? extends Annotation> annotation : annotations) {
                    if (!annotationList.isAnnotationPresent(annotation)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * 匹配由{@link Object}声明的方法
     *
     * @return 方法匹配器对象
     */
    public static MethodMatcher isDeclaredByObject() {
        return isDeclaredBy(Object.class);
    }

    /**
     * 匹配由传入类声明的方法
     *
     * @param cls 类
     * @return 方法匹配器对象
     */
    public static MethodMatcher isDeclaredBy(Class<?> cls) {
        return build(ElementMatchers.isDeclaredBy(cls));
    }

    /**
     * 匹配静态方法，见{@link #methodTypeMatches}
     *
     * @return 方法匹配器对象
     */
    public static MethodMatcher isStaticMethod() {
        return methodTypeMatches(MethodType.STATIC);
    }

    /**
     * 匹配构造方法，见{@link #methodTypeMatches}
     *
     * @return 方法匹配器对象
     */
    public static MethodMatcher isConstructor() {
        return methodTypeMatches(MethodType.CONSTRUCTOR);
    }

    /**
     * 匹配成员方法，见{@link #methodTypeMatches}
     *
     * @return 方法匹配器对象
     */
    public static MethodMatcher isMemberMethod() {
        return methodTypeMatches(MethodType.MEMBER);
    }

    /**
     * 匹配符合类型的方法，包括静态方法，构造函数和成员方法三种
     *
     * @param methodType 方法类型
     * @return 方法匹配器对象
     */
    public static MethodMatcher methodTypeMatches(MethodType methodType) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return methodType.match(methodDescription);
            }
        };
    }

    /**
     * 匹配参数数量一致的方法
     *
     * @param count 数量
     * @return 方法匹配器对象
     */
    public static MethodMatcher paramCountEquals(int count) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return count == methodDescription.getParameters().size();
            }
        };
    }

    /**
     * 匹配参数类型一致的方法
     *
     * @param paramTypes 参数类型集
     * @return 方法匹配器对象
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public static MethodMatcher paramTypesEqual(String... paramTypes) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                final ParameterList<?> parameters = methodDescription.getParameters();
                if (paramTypes.length != parameters.size()) {
                    return false;
                }
                for (int i = 0; i < paramTypes.length; i++) {
                    if (!parameters.get(i).getType().asErasure().getActualName().equals(paramTypes[i])) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * 匹配参数类型一致的方法
     *
     * @param paramTypes 参数类型集
     * @return 方法匹配器对象
     */
    public static MethodMatcher paramTypesEqual(Class<?>... paramTypes) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                final ParameterList<?> parameters = methodDescription.getParameters();
                if (paramTypes.length != parameters.size()) {
                    return false;
                }
                for (int i = 0; i < paramTypes.length; i++) {
                    if (!parameters.get(i).getType().asErasure().getActualName().equals(paramTypes[i].getName())) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * 匹配返回值类型一致的方法
     *
     * @param resultType 返回值类型
     * @return 方法匹配器对象
     */
    public static MethodMatcher resultTypeEquals(String resultType) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return methodDescription.getReturnType().asErasure().getActualName().equals(resultType);
            }
        };
    }

    /**
     * 匹配返回值类型一致的方法
     *
     * @param resultType 返回值类型
     * @return 方法匹配器对象
     */
    public static MethodMatcher resultTypeEquals(Class<?> resultType) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return methodDescription.getReturnType().asErasure().getActualName().equals(resultType.getName());
            }
        };
    }

    /**
     * 通过byte-buddy的元素匹配器构建方法匹配器对象
     *
     * @param elementMatcher 元素匹配器
     * @return 方法匹配器对象
     */
    public static MethodMatcher build(ElementMatcher<MethodDescription> elementMatcher) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return elementMatcher.matches(methodDescription);
            }
        };
    }

    /**
     * 逻辑操作{@code not}，方法匹配器集全为假时返回真，否则返回假
     *
     * @param matchers 方法匹配器集
     * @return 方法匹配器对象
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    @SafeVarargs
    public static MethodMatcher not(ElementMatcher<MethodDescription>... matchers) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                for (ElementMatcher<MethodDescription> matcher : matchers) {
                    if (matcher.matches(methodDescription)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * 逻辑操作{@code not}，原为假时返回真，否则返回假
     *
     * @return 方法匹配器对象
     */
    public MethodMatcher not() {
        final MethodMatcher thisMatcher = this;
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return !thisMatcher.matches(methodDescription);
            }
        };
    }

    /**
     * 逻辑操作{@code and}，方法匹配器集全为真时返回真，否则返回假
     *
     * @param matchers 方法匹配器集
     * @return 方法匹配器对象
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    @SafeVarargs
    public static MethodMatcher and(ElementMatcher<MethodDescription>... matchers) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                for (ElementMatcher<MethodDescription> matcher : matchers) {
                    if (!matcher.matches(methodDescription)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * 逻辑操作{@code and}，同为真时返回真，否则返回假
     *
     * @param matcher 另一个方法匹配器
     * @return 方法匹配器对象
     */
    public MethodMatcher and(ElementMatcher<MethodDescription> matcher) {
        final MethodMatcher thisMatcher = this;
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return thisMatcher.matches(methodDescription) && matcher.matches(methodDescription);
            }
        };
    }

    /**
     * 逻辑操作{@code or}，方法匹配器集其中一个为真时返回真，否则返回假
     *
     * @param matchers 方法匹配器集
     * @return 方法匹配器对象
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    @SafeVarargs
    public static MethodMatcher or(ElementMatcher<MethodDescription>... matchers) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                for (ElementMatcher<MethodDescription> matcher : matchers) {
                    if (matcher.matches(methodDescription)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * 逻辑操作{@code or}，两者其一为真时返回真，否则返回假
     *
     * @param matcher 另一个方法匹配器
     * @return 方法匹配器对象
     */
    public MethodMatcher or(ElementMatcher<MethodDescription> matcher) {
        final MethodMatcher thisMatcher = this;
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return thisMatcher.matches(methodDescription) || matcher.matches(methodDescription);
            }
        };
    }
}