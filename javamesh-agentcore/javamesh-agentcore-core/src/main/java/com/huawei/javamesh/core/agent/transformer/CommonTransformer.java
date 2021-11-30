/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.core.agent.transformer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

/**
 * 通用转换器
 * <p>当被增强类为启动类加载器加载的类时，移交{@link BootstrapTransformer}处理，否则移交{@link DelegateTransformer}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public class CommonTransformer implements AgentBuilder.Transformer {
    private final BootstrapTransformer bootstrapTransformer;
    private final DelegateTransformer delegateTransformer;

    public CommonTransformer() {
        this.bootstrapTransformer = new BootstrapTransformer();
        this.delegateTransformer = new DelegateTransformer();
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule module) {
        if (classLoader == null) {
            return bootstrapTransformer.transform(builder, typeDescription, null, module);
        } else {
            return delegateTransformer.transform(builder, typeDescription, classLoader, module);
        }
    }
}
