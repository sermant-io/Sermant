/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.core.agent.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;

import com.huawei.javamesh.core.agent.definition.EnhanceDefinition;
import com.huawei.javamesh.core.agent.definition.MethodInterceptPoint;
import com.huawei.javamesh.core.lubanops.bootstrap.Listener;
import com.huawei.javamesh.core.lubanops.bootstrap.TransformerMethod;

/**
 * 方法的相关拦截器的信息收集器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/27
 */
public class MethodInterceptorCollector {
    /**
     * 被增强类的方法
     */
    protected final MethodDescription.InDefinedShape method;

    /**
     * luban插件增强当前方法的拦截器名称
     */
    protected String originInterceptorName;

    /**
     * 增强当前方法的拦截器名称
     */
    protected final List<String> interceptorNames;

    protected MethodInterceptorCollector(MethodDescription.InDefinedShape method) {
        this.method = method;
        this.interceptorNames = new ArrayList<String>();
    }

    /**
     * 添加luban插件增强当前方法的拦截器
     *
     * @param listener luban的监听器
     */
    protected boolean setOriginInterceptor(Listener listener) {
        if (originInterceptorName != null) {
            return false;
        }
        final List<TransformerMethod> transformerMethods = listener.getTransformerMethod();
        if (transformerMethods == null) {
            return false;
        }
        for (TransformerMethod transformerMethod : transformerMethods) {
            if (isMethodMatch(transformerMethod)) {
                listener.addTag();
                originInterceptorName = transformerMethod.getInterceptor();
                return true;
            }
        }
        return false;
    }

    /**
     * 执行添加luban插件增强当前方法的拦截器
     *
     * @param transformerMethod luban方法增强信息的包装类
     * @return 方法是否匹配
     */
    private boolean isMethodMatch(TransformerMethod transformerMethod) {
        if (transformerMethod.getMethod() == null) {
            final Set<String> excludeMethods = transformerMethod.getExcludeMethods();
            return excludeMethods != null && !excludeMethods.contains(method.getName());
        } else {
            // 同为构造函数且参数列表一致、或同为普通方法且方法名和参数列表一致时，通过
            return ((method.isConstructor() && transformerMethod.isConstructor()) ||
                    (!method.isConstructor() && !transformerMethod.isConstructor() &&
                            method.getName().equals(transformerMethod.getMethod()))) &&
                    (isParamTypeMatch(transformerMethod));
        }
    }

    /**
     * 检查参数类型是否匹配，当luban监听器中没有声明参数类型时同样表示匹配
     *
     * @param transformerMethod luban声明的方法对象
     * @return 参数类型是否匹配
     */
    private boolean isParamTypeMatch(TransformerMethod transformerMethod) {
        final List<String> params = transformerMethod.getParams();
        if (params.isEmpty()) {
            return true;
        }
        final ParameterList<ParameterDescription.InDefinedShape> parameters = method.getParameters();
        if (params.size() != parameters.size()) {
            return false;
        }
        for (int i = 0; i < params.size(); i++) {
            if (!params.get(i).equals(parameters.get(i).getType().getTypeName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 添加当前方法的拦截器
     *
     * @param enhanceDefinitions 增强定义列表
     */
    protected void addInterceptors(List<EnhanceDefinition> enhanceDefinitions) {
        for (EnhanceDefinition definition : enhanceDefinitions) {
            for (MethodInterceptPoint point : definition.getMethodInterceptPoints()) {
                // 方法类型相同且满足匹配条件时通过
                if (((point.isStaticMethod() && method.isStatic()) ||
                        (point.isConstructor() && method.isConstructor()) ||
                        (point.isInstanceMethod() && !method.isStatic() && !method.isConstructor())) &&
                        (point.getMatcher().matches(method))) {
                    interceptorNames.add(point.getInterceptor());
                }
            }
        }
    }
}
