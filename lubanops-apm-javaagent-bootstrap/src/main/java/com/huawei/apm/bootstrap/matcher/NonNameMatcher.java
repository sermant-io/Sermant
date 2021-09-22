package com.huawei.apm.bootstrap.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 非类名匹配器接口
 */
public interface NonNameMatcher extends ClassMatcher {

    /**
     * 构造类型匹配条件连接点
     *
     * @return 匹配条件连接点
     */
    ElementMatcher.Junction<TypeDescription> buildJunction();

    /**
     * 判断目标类型是否匹配
     *
     * @param typeDescription 目标类型
     * @return 如果匹配返回true，否则false
     */
    boolean isMatch(TypeDescription typeDescription);
}
