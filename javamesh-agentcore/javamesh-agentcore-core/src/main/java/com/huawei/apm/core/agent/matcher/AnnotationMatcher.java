package com.huawei.apm.core.agent.matcher;

import com.huawei.apm.core.util.Assert;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * 注解匹配器
 */
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
