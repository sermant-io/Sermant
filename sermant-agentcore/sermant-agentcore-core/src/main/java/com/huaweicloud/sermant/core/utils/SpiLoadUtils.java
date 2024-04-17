/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.sermant.core.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * SpiLoadUtils
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-16
 */
public class SpiLoadUtils {
    private SpiLoadUtils() {
    }

    /**
     * 从指定类加载起载入所有的服务
     *
     * @param serviceClass SPI接口类
     * @param classLoader  类加载器，从指定的类加载加载服务
     * @param <T>          服务的具体类型
     * @return 服务列表
     */
    public static <T> List<T> loadAll(Class<T> serviceClass, ClassLoader classLoader) {
        ServiceLoader<T> services = ServiceLoader.load(serviceClass, classLoader);
        List<T> list = new ArrayList<>();
        for (T service : services) {
            list.add(service);
        }
        list.sort(Comparator.comparingInt(obj -> {
            SpiWeight weight = obj.getClass().getAnnotation(SpiWeight.class);
            return weight.value();
        }));
        return list;
    }

    /**
     * Get the optimal implementation by weight
     *
     * @param clazz spi target class
     * @param <T> target type
     * @return best implementation
     */
    public static <T> T getBestImpl(Class<T> clazz) {
        return getBestImpl(clazz, ClassLoader.getSystemClassLoader());
    }

    /**
     * Get the optimal implementation by weight
     *
     * @param clazz spi target class
     * @param classLoader classLoader for searching
     * @param <T> target type
     * @return best implementation
     */
    public static <T> T getBestImpl(Class<T> clazz, ClassLoader classLoader) {
        T impl = null;
        for (T newImpl : ServiceLoader.load(clazz, classLoader)) {
            impl = getBetter(impl, newImpl);
        }
        return impl;
    }

    /**
     * Compare weights, taking the latter when returning true and the former otherwise
     *
     * @param src source
     * @param dst destination
     * @param <T> type
     * @return result
     */
    public static <T> T getBetter(T src, T dst) {
        return getBetter(src, dst, new WeightEqualHandler<T>() {
            @Override
            public T handle(T source, T target) {
                return source;
            }
        });
    }

    /**
     * Compare weights, taking the latter when returning true and the former otherwise
     *
     * @param src source
     * @param dst destination
     * @param handler WeightEqualHandler
     * @param <T> type
     * @return result
     */
    public static <T> T getBetter(T src, T dst, WeightEqualHandler<T> handler) {
        if (dst == null) {
            return src;
        } else if (src == null) {
            return dst;
        } else {
            final SpiWeight srcWeight = src.getClass().getAnnotation(SpiWeight.class);
            final int srcWeightVal = srcWeight == null ? Integer.MIN_VALUE : srcWeight.value();
            final SpiWeight dstWeight = dst.getClass().getAnnotation(SpiWeight.class);
            final int dstWeightVal = dstWeight == null ? Integer.MIN_VALUE : dstWeight.value();
            if (srcWeightVal > dstWeightVal) {
                return src;
            } else if (srcWeightVal < dstWeightVal) {
                return dst;
            } else {
                return handler.handle(src, dst);
            }
        }
    }

    /**
     * spi weight
     *
     * @since 2021-11-16
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface SpiWeight {
        /**
         * weight value
         *
         * @return value
         */
        int value() default 0;
    }

    /**
     * WeightEqualHandler
     *
     * @param <T> type
     *
     * @since 2021-11-16
     */
    public interface WeightEqualHandler<T> {
        /**
         * Handle the same method, choose one of them
         *
         * @param source source
         * @param target target
         * @return result
         */
        T handle(T source, T target);
    }
}
