package com.huawei.javamesh.core.agent.definition;

import com.huawei.javamesh.core.agent.matcher.ClassMatcher;

/**
 * 增强定义
 */
public interface EnhanceDefinition {

    /**
     * 获取待增强的目标类
     *
     * @return 待增强的目标类型匹配器
     */
    ClassMatcher enhanceClass();

    /**
     * 获取封装了待增强目标方法和其拦截器的MethodInterceptPoint
     *
     * @return MethodInterceptPoint数组
     */
    MethodInterceptPoint[] getMethodInterceptPoints();

}
