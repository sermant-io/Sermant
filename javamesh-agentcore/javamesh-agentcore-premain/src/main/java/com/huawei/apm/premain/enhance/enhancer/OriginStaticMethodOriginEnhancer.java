package com.huawei.apm.premain.enhance.enhancer;

import com.huawei.apm.premain.common.OverrideArgumentsCall;
import com.huawei.apm.bootstrap.lubanops.Interceptor;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;

/**
 * 针对原插件静态方法的处理
 */
public final class OriginStaticMethodOriginEnhancer extends OriginEnhancer {
    public OriginStaticMethodOriginEnhancer(Interceptor interceptor) {
        super(interceptor);
    }

    @RuntimeType
    public Object intercept(
            @Origin Class<?> clazz,
            @AllArguments Object[] allArguments,
            @Morph OverrideArgumentsCall callable,
            @Origin Method method) throws Exception {
        return process(clazz, allArguments, callable, method);
    }
}
