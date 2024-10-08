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
 * ClassMatcher, contains two types:
 * <pre>
 *     1.Class type matcher, {@link ClassTypeMatcher}
 *     1.Class fuzzy matcher, {@link ClassFuzzyMatcher}
 * </pre>
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public abstract class ClassMatcher implements ElementMatcher<TypeDescription> {
    /**
     * Match classes with exact name
     *
     * @param typeName Class fully qualified name
     * @return ClassTypeMatcher
     */
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
     * Match multiple classes
     *
     * @param typeNames Class fully qualified name set
     * @return ClassTypeMatcher
     */
    public static ClassTypeMatcher nameContains(String... typeNames) {
        return nameContains(new HashSet<>(Arrays.asList(typeNames)));
    }

    /**
     * Match multiple classes
     *
     * @param typeNames Class fully qualified name set
     * @return ClassTypeMatcher
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
     * Match classes whose class name prefix meets the requirements
     *
     * @param prefix prefix
     * @return ClassFuzzyMatcher
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
     * Match classes whose class name suffix meet the requirements
     *
     * @param suffix suffix
     * @return ClassFuzzyMatcher
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
     * Match classes whose class name infix meet the requirements
     *
     * @param infix infix
     * @return ClassFuzzyMatcher
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
     * Matches classes that satisfy regular expression
     *
     * @param pattern Regular expression
     * @return ClassFuzzyMatcher
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
     * Match classes by class annotation
     *
     * @param annotations annotation set
     * @return ClassFuzzyMatcher
     */
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
     * Match classes by class annotation
     *
     * @param annotations annotation set
     * @return ClassFuzzyMatcher
     */
    @SafeVarargs
    public static ClassFuzzyMatcher isAnnotatedWith(Class<? extends Annotation>... annotations) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return isAnnotatedWithMatch(typeDescription, annotations);
            }
        };
    }

    /**
     * Match classes that inherit the specific superclass
     *
     * @param superTypes superclass type set
     * @return ClassFuzzyMatcher
     */
    public static ClassFuzzyMatcher isExtendedFrom(String... superTypes) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return superTypeCheck(typeDescription, Arrays.asList(superTypes));
            }
        };
    }

    /**
     * Match classes that inherit the specific superclass
     *
     * @param superTypes superclass type set
     * @return ClassFuzzyMatcher
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
     * superclass check
     *
     * @param typeDescription type description
     * @param superTypeNames superclass type name set
     * @return check result
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
     * Build ClassFuzzyMatcher through byte-buddy's ElementMatcher
     *
     * @param elementMatcher Element matcher
     * @return ClassFuzzyMatcher
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
     * Logical operation {@code not}, returns true if the ClassMatcher set is all false, and false otherwise
     *
     * @param matchers ClassMatcher set
     * @return ClassFuzzyMatcher
     */
    public static ClassFuzzyMatcher not(ClassMatcher... matchers) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return notMatch(typeDescription, matchers);
            }
        };
    }

    /**
     * Logical operation {@code and}, returns true if the ClassMatcher set is all true, and false otherwise
     *
     * @param matchers ClassMatcher set
     * @return ClassFuzzyMatcher
     */
    public static ClassFuzzyMatcher and(ClassMatcher... matchers) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return andMatch(typeDescription, matchers);
            }
        };
    }

    /**
     * Logical operation {@code or}, returns true if one of ClassMatchers is true, and false otherwise
     *
     * @param matchers ClassMatcher set
     * @return ClassFuzzyMatcher
     */
    public static ClassFuzzyMatcher or(ClassMatcher... matchers) {
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return orMatch(typeDescription, matchers);
            }
        };
    }

    private static boolean orMatch(TypeDescription typeDescription, ClassMatcher... matchers) {
        for (ClassMatcher matcher : matchers) {
            if (matcher.matches(typeDescription)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnnotatedWithMatch(TypeDescription typeDescription,
            Class<? extends Annotation>... annotations) {
        final AnnotationList annotationList = typeDescription.getInheritedAnnotations();
        for (Class<? extends Annotation> annotation : annotations) {
            if (!annotationList.isAnnotationPresent(annotation)) {
                return false;
            }
        }
        return true;
    }

    private static boolean notMatch(TypeDescription typeDescription, ClassMatcher... matchers) {
        for (ClassMatcher matcher : matchers) {
            if (matcher.matches(typeDescription)) {
                return false;
            }
        }
        return true;
    }

    private static boolean andMatch(TypeDescription typeDescription, ClassMatcher... matchers) {
        for (ClassMatcher matcher : matchers) {
            if (!matcher.matches(typeDescription)) {
                return false;
            }
        }
        return true;
    }
}
