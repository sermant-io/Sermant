package com.huawei.apm.bootstrap.matcher;

/**
 * 类名匹配器
 */
public class NameMatcher implements ClassMatcher {

    private final String className;

    public NameMatcher(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
