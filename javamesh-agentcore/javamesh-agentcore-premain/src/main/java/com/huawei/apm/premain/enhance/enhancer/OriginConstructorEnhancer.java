package com.huawei.apm.premain.enhance.enhancer;

import com.huawei.apm.bootstrap.lubanops.Interceptor;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * 适配原生构造方法委托类
 */
public final class OriginConstructorEnhancer extends OriginEnhancer {

    public OriginConstructorEnhancer(Interceptor interceptor) {
        super(interceptor);
    }

    @RuntimeType
    public void intercept(@This Object obj,
            @AllArguments Object[] allArguments) {
        String className = obj.getClass().getName();
        String methodName = "constructor";
        try {
            Object[] newArguments = interceptor.onStart(obj, allArguments, className, methodName);
            if (newArguments != null && newArguments.length == allArguments.length) {
                allArguments = newArguments;
            }
        } catch (Throwable t) {
            LogFactory.getLogger().severe(String.format("invoke construct onStart method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                    className, methodName, t.getMessage()));
        }
        try {
            interceptor.onFinally(obj, allArguments, null, obj.getClass().getName(), "constructor");
        } catch (Throwable t) {
            LogFactory.getLogger().severe(String.format("invoke construct onFinally method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                    className, methodName, t.getMessage()));
        }
    }

}
