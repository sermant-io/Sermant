package com.huawei.apm.enhance.enhancer;

import com.huawei.apm.common.OverrideArgumentsCall;
import com.lubanops.apm.bootstrap.Interceptor;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;

/**
 * 针对原插件实例方法的处理
 */
public final class OriginInstanceMethodOriginEnhancer extends OriginEnhancer {
    public OriginInstanceMethodOriginEnhancer(Interceptor interceptor) {
        super(interceptor);
    }

    @RuntimeType
    public Object intercept(@This Object obj,
            @AllArguments Object[] allArguments,
            @Morph OverrideArgumentsCall callable,
            @Origin Method method) throws Exception {
        return process(obj, allArguments, callable, method);
    }
}
