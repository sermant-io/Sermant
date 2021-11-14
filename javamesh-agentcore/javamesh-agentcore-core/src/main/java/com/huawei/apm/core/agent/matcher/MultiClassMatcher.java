package com.huawei.apm.core.agent.matcher;

import com.huawei.apm.core.util.Assert;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 多个类匹配
 */
public class MultiClassMatcher implements NonNameMatcher {

    private final Set<String> classNames;

    MultiClassMatcher(String[] classNames) {
        Assert.notEmpty(classNames, "Class names must not be empty.");
        this.classNames = new HashSet<String>(Arrays.asList(classNames));
    }

    @Override
    public ElementMatcher.Junction<TypeDescription> buildJunction() {
        return new ElementMatcher.Junction.AbstractBase<TypeDescription>() {
            @Override
            public boolean matches(TypeDescription target) {
                return classNames.contains(target.getActualName());
            }
        };
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        return classNames.contains(typeDescription.getActualName());
    }
}
