package com.huawei.apm.bootstrap.matcher;

import com.huawei.apm.bootstrap.util.Assert;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 类名前缀匹配器
 */
public class PrefixMatcher implements NonNameMatcher {

    private final String prefix;

    public PrefixMatcher(String prefix) {
        Assert.hasText(prefix, "Prefix can not be blank.");
        this.prefix = prefix;
    }

    @Override
    public ElementMatcher.Junction<TypeDescription> buildJunction() {
        return ElementMatchers.nameStartsWith(prefix);
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        return typeDescription.getActualName().startsWith(prefix);
    }
}
