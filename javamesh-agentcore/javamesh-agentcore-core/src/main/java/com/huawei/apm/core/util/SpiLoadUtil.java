package com.huawei.apm.core.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ServiceLoader;

/**
 * 插件spi加载工具
 */
public class SpiLoadUtil {
    public static <T> T getImpl(Class<T> clazz, ClassLoader classLoader) {
        T impl = null;
        for (T newImpl : ServiceLoader.load(clazz, classLoader)) {
            impl = compare(impl, newImpl) ? impl : newImpl;
        }
        return impl;
    }

    public static <T> boolean compare(T source, T target) {
        if (target == null) {
            return true;
        } else if (source == null) {
            return false;
        } else {
            final SpiWeight sourceWeight = source.getClass().getAnnotation(SpiWeight.class);
            final SpiWeight targetWeight = target.getClass().getAnnotation(SpiWeight.class);
            if (targetWeight == null) {
                return true;
            } else if (sourceWeight == null) {
                return false;
            } else {
                return sourceWeight.value() >= targetWeight.value();
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface SpiWeight {
        int value() default 0;
    }
}
