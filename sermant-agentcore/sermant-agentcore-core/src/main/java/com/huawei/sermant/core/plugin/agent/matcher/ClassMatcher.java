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
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * 类的匹配器，分为两种类型：
 * <pre>
 *     1.类的类型匹配器，将{@link ClassTypeMatcher}
 *     1.类的模糊匹配器，将{@link ClassFuzzyMatcher}
 * </pre>
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public abstract class ClassMatcher implements ElementMatcher<TypeDescription> {
    /**
     * 匹配名称完全一致的类
     *
     * @param typeName 类全限定名
     * @return 类的类型匹配器
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public static ClassTypeMatcher nameEquals(String typeName) {
        return new ClassTypeMatcher() {
            @Override
            public Set<String> getTypeNames() {
                return Collections.singleton(typeName);
            }

            @Override
            public boolean matches(TypeDescription typeDescription) {
                return typeDescription.getActualName().equals(typeName);
            }
        };
    }

    /**
     * 匹配多个类
     *
     * @param typeNames 类全限定名集
     * @return 类的类型匹配器
     */
    public static ClassTypeMatcher nameContains(String... typeNames) {
        return nameContains(new HashSet<>(Arrays.asList(typeNames)));
    }

    /**
     * 匹配多个类
     *
     * @param typeNames 类全限定名集
     * @return 类的类型匹配器
     */
    public static ClassTypeMatcher nameContains(Set<String> typeNames) {
        return new ClassTypeMatcher() {
            @Override
            public Set<String> getTypeNames() {
                return typeNames;
            }

            @Override
            public boolean matches(TypeDescription typeDescription) {
                return typeNames.contains(typeDescription.getActualName());
            }
        };
    }

    /**
     * 匹配类名前缀满足要求的类
     *
     * @param prefix 前缀
     * @return 类的模糊匹配器
     */
    public static ClassFuzzyMatcher namePrefixedWith(String prefix) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return typeDescription.getActualName().startsWith(prefix);
            }
        };
    }

    /**
     * 匹配类名后缀满足要求的类
     *
     * @param suffix 后缀
     * @return 类的模糊匹配器
     */
    public static ClassFuzzyMatcher nameSuffixedWith(String suffix) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return typeDescription.getActualName().endsWith(suffix);
            }
        };
    }

    /**
     * 匹配类名内缀满足要求的类
     *
     * @param infix 内缀
     * @return 类的模糊匹配器
     */
    public static ClassFuzzyMatcher nameInfixedWith(String infix) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return typeDescription.getActualName().contains(infix);
            }
        };
    }

    /**
     * 匹配满足正则表达式的类
     *
     * @param pattern 正则表达式
     * @return 类的模糊匹配器
     */
    public static ClassFuzzyMatcher nameMatches(String pattern) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return typeDescription.getActualName().matches(pattern);
            }
        };
    }

    /**
     * 匹配被传入所有注解修饰的类
     *
     * @param annotations 注解集
     * @return 类的模糊匹配器
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public static ClassFuzzyMatcher isAnnotatedWith(String... annotations) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                final Set<String> annotationSet = new HashSet<String>(Arrays.asList(annotations));
                final AnnotationList annotationList = typeDescription.getInheritedAnnotations();
                for (AnnotationDescription description : annotationList) {
                    annotationSet.remove(description.getAnnotationType().getActualName());
                }
                return annotationSet.isEmpty();
            }
        };
    }

    /**
     * 匹配被传入所有注解修饰的类
     *
     * @param annotations 注解集
     * @return 类的模糊匹配器
     */
    @SafeVarargs
    public static ClassFuzzyMatcher isAnnotatedWith(Class<? extends Annotation>... annotations) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                final AnnotationList annotationList = typeDescription.getInheritedAnnotations();
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
     * 匹配继承传入所有超类的类
     *
     * @param superTypes 超类集
     * @return 类的模糊匹配器
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public static ClassFuzzyMatcher isExtendedFrom(String... superTypes) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return superTypeCheck(typeDescription, Arrays.asList(superTypes));
            }
        };
    }

    /**
     * 匹配继承传入所有超类的类
     *
     * @param superTypes 超类集
     * @return 类的模糊匹配器
     */
    public static ClassFuzzyMatcher isExtendedFrom(Class<?>... superTypes) {
        final Set<String> superTypeNames = new HashSet<String>();
        for (Class<?> superType : superTypes) {
            superTypeNames.add(superType.getName());
        }
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return superTypeCheck(typeDescription, superTypeNames);
            }
        };
    }

    /**
     * 超类检查
     *
     * @param typeDescription 类型描述
     * @param superTypeNames  超类名称集
     * @return 检查是否通过
     */
    private static boolean superTypeCheck(TypeDescription typeDescription, Collection<String> superTypeNames) {
        final Set<String> superTypeNameSet = new HashSet<String>(superTypeNames);
        if (superTypeNameSet.contains(typeDescription.asErasure().getActualName())) {
            return false;
        }
        final Queue<TypeDefinition> queue = new LinkedList<TypeDefinition>();
        queue.add(typeDescription);
        for (TypeDefinition current = queue.poll();
             current != null && !superTypeNameSet.isEmpty();
             current = queue.poll()) {
            superTypeNameSet.remove(current.getActualName());
            final TypeList.Generic interfaces = current.getInterfaces();
            if (!interfaces.isEmpty()) {
                queue.addAll(interfaces.asErasures());
            }
            final TypeDefinition superClass = current.getSuperClass();
            if (superClass != null) {
                queue.add(superClass.asErasure());
            }
        }
        return superTypeNameSet.isEmpty();
    }

    /**
     * 通过byte-buddy的元素匹配器构建类的模糊匹配器
     *
     * @param elementMatcher 元素匹配器
     * @return 类的模糊匹配器
     */
    public static ClassFuzzyMatcher build(ElementMatcher<TypeDescription> elementMatcher) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return elementMatcher.matches(typeDescription);
            }
        };
    }

    /**
     * 逻辑操作{@code not}，类的匹配器集全为假时返回真，否则返回假
     *
     * @param matchers 类的匹配器集
     * @return 类的模糊匹配器
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public static ClassFuzzyMatcher not(ClassMatcher... matchers) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                for (ClassMatcher matcher : matchers) {
                    if (matcher.matches(typeDescription)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * 逻辑操作{@code and}，类的匹配器集全为真时返回真，否则返回假
     *
     * @param matchers 类的匹配器集
     * @return 类的模糊匹配器
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public static ClassFuzzyMatcher and(ClassMatcher... matchers) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                for (ClassMatcher matcher : matchers) {
                    if (!matcher.matches(typeDescription)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * 逻辑操作{@code or}，类的匹配器集其中一个为真时返回真，否则返回假
     *
     * @param matchers 类的匹配器集
     * @return 类的模糊匹配器
     */
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public static ClassFuzzyMatcher or(ClassMatcher... matchers) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                for (ClassMatcher matcher : matchers) {
                    if (matcher.matches(typeDescription)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}