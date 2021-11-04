package com.huawei.apm.core.agent.matcher;

/**
 * 匹配器Facade类
 */
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
