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

package io.sermant.core.plugin.agent.matcher;

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
 * MethodMatcher
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public abstract class MethodMatcher implements ElementMatcher<MethodDescription> {
    /**
     * Match any methods
     *
     * @return MethodMatcher
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
     * Match methods with exact name
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
     * Match multiple methods
     *
     * @param methodNames method name set
     * @return MethodMatcher
     */
    public static MethodMatcher nameContains(String... methodNames) {
        return nameContains(new HashSet<>(Arrays.asList(methodNames)));
    }

    /**
     * Match multiple methods
     *
     * @param methodNames method name set
     * @return MethodMatcher
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
     * Match methods whose method name prefix meets the requirement
     *
     * @param prefix prefix
     * @return MethodMatcher
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
     * Match methods whose method name suffix meets the requirement
     *
     * @param suffix suffix
     * @return MethodMatcher
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
     * Match methods whose method name infix meets the requirement
     *
     * @param infix infix
     * @return MethodMatcher
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
     * Match methods that satisfy regular expression
     *
     * @param pattern regular expression
     * @return MethodMatcher
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
     * Match methods by method annotations
     *
     * @param annotations annotation set
     * @return MethodMatcher
     */
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
     * Match methods by method annotations
     *
     * @param annotations annotation set
     * @return MethodMatcher
     */
    @SafeVarargs
    public static MethodMatcher isAnnotatedWith(Class<? extends Annotation>... annotations) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return isAnnotatedWithMatch(methodDescription, annotations);
            }
        };
    }

    /**
     * Match methods declared by {@link Object}
     *
     * @return MethodMatcher
     */
    public static MethodMatcher isDeclaredByObject() {
        return isDeclaredBy(Object.class);
    }

    /**
     * Match methods declared by specific class
     *
     * @param cls class
     * @return MethodMatcher
     */
    public static MethodMatcher isDeclaredBy(Class<?> cls) {
        return build(ElementMatchers.isDeclaredBy(cls));
    }

    /**
     * Match static methods, see {@link #methodTypeMatches}
     *
     * @return MethodMatcher
     */
    public static MethodMatcher isStaticMethod() {
        return methodTypeMatches(MethodType.STATIC);
    }

    /**
     * Match constructors, see {@link #methodTypeMatches}
     *
     * @return MethodMatcher
     */
    public static MethodMatcher isConstructor() {
        return methodTypeMatches(MethodType.CONSTRUCTOR);
    }

    /**
     * Match member methods, see {@link #methodTypeMatches}
     *
     * @return MethodMatcher
     */
    public static MethodMatcher isMemberMethod() {
        return methodTypeMatches(MethodType.MEMBER);
    }

    /**
     * Match public methods, see {@link #methodTypeMatches}
     *
     * @return MethodMatcher
     */
    public static MethodMatcher isPublicMethod() {
        return methodTypeMatches(MethodType.PUBLIC);
    }

    /**
     * Match methods with type, including static methods, constructors, and member methods
     *
     * @param methodType method type
     * @return MethodMatcher
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
     * Match methods with the same number of parameters
     *
     * @param count parameter count
     * @return MethodMatcher
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
     * Match methods with the same parameter types
     *
     * @param paramTypes parameter type set
     * @return MethodMatcher
     */
    public static MethodMatcher paramTypesEqual(String... paramTypes) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return paramTypesEqualMatch(methodDescription, paramTypes);
            }
        };
    }

    /**
     * Match methods with the same parameter types
     *
     * @param paramTypes parameter type set
     * @return MethodMatcher
     */
    public static MethodMatcher paramTypesEqual(Class<?>... paramTypes) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return paramTypesEqualMatch(methodDescription, paramTypes);
            }
        };
    }

    /**
     * Match methods that return value of the same type
     *
     * @param resultType result type
     * @return MethodMatcher
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
     * Match methods that return value of the same type
     *
     * @param resultType result type
     * @return MethodMatcher
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
     * Build MethodMatcher objects through byte-buddy's ElementMatcher
     *
     * @param elementMatcher element matcher
     * @return MethodMatcher
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
     * Logical operation {@code not}, returns true if the MethodMatcher set is all false, and false otherwise
     *
     * @param matchers MethodMatcher set
     * @return MethodMatcher
     */
    @SafeVarargs
    public static MethodMatcher not(ElementMatcher<MethodDescription>... matchers) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return notMatch(methodDescription, matchers);
            }
        };
    }

    /**
     * Logical operation {@code not}
     *
     * @return MethodMatcher
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
     * Logical operation {@code and}, returns true if the MethodMatcher set is all true, and false otherwise
     *
     * @param matchers MethodMatcher set
     * @return MethodMatcher
     */
    @SafeVarargs
    public static MethodMatcher and(ElementMatcher<MethodDescription>... matchers) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return andMatch(methodDescription, matchers);
            }
        };
    }

    /**
     * Logical operation {@code and}, returns true if both MethodMatchers is all true, and false otherwise
     *
     * @param matcher MethodMatcher
     * @return MethodMatcher
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
     * Logical operation {@code or}, returns true if one of MethodMatchers is true, and false otherwise
     *
     * @param matchers MethodMatcher set
     * @return MethodMatcher
     */
    @SafeVarargs
    public static MethodMatcher or(ElementMatcher<MethodDescription>... matchers) {
        return new MethodMatcher() {
            @Override
            public boolean matches(MethodDescription methodDescription) {
                return orMatch(methodDescription, matchers);
            }
        };
    }

    /**
     * Logical operation {@code or}, returns true if one of MethodMatchers is true, and false otherwise
     *
     * @param matcher MethodMatcher set
     * @return MethodMatcher
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

    private static boolean isAnnotatedWithMatch(MethodDescription methodDescription,
            Class<? extends Annotation>... annotations) {
        final AnnotationList annotationList = methodDescription.getDeclaredAnnotations();
        for (Class<? extends Annotation> annotation : annotations) {
            if (!annotationList.isAnnotationPresent(annotation)) {
                return false;
            }
        }
        return true;
    }

    private static boolean paramTypesEqualMatch(MethodDescription methodDescription, String... paramTypes) {
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

    private static boolean paramTypesEqualMatch(MethodDescription methodDescription, Class<?>... paramTypes) {
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

    private static boolean notMatch(MethodDescription methodDescription,
            ElementMatcher<MethodDescription>... matchers) {
        for (ElementMatcher<MethodDescription> matcher : matchers) {
            if (matcher.matches(methodDescription)) {
                return false;
            }
        }
        return true;
    }

    private static boolean andMatch(MethodDescription methodDescription,
            ElementMatcher<MethodDescription>... matchers) {
        for (ElementMatcher<MethodDescription> matcher : matchers) {
            if (!matcher.matches(methodDescription)) {
                return false;
            }
        }
        return true;
    }

    private static boolean orMatch(MethodDescription methodDescription,
            ElementMatcher<MethodDescription>... matchers) {
        for (ElementMatcher<MethodDescription> matcher : matchers) {
            if (matcher.matches(methodDescription)) {
                return true;
            }
        }
        return false;
    }
}