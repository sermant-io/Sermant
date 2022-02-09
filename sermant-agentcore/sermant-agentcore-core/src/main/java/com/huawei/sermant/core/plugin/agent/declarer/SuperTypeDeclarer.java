/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.plugin.agent.declarer;

import com.huawei.sermant.core.plugin.agent.annotations.BeanPropertyFlag;
import com.huawei.sermant.core.plugin.agent.template.ImplDelegator;

import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 超类声明器，禁用自定义实现，仅提供一下两种类型的实现：
 * <pre>
 *     1.使用接口实现去实现超类接口
 *     2.依{@link BeanPropertyFlag#value()}值生成字段和get、set方法
 * </pre>
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public abstract class SuperTypeDeclarer {
    /**
     * 禁用构造函数，因此，禁用外界自定义实现
     */
    private SuperTypeDeclarer() {
    }

    /**
     * 获取超类类型，籍由判断是否处理增强
     *
     * @return 超类类型
     */
    public abstract Class<?> getSuperType();

    /**
     * 处理增强
     *
     * @param superType 超类类型
     * @param builder   构建器
     * @return 构建器
     */
    public abstract DynamicType.Builder<?> resolve(Class<?> superType, DynamicType.Builder<?> builder);

    /**
     * 使用接口实现去实现超类接口的{@link SuperTypeDeclarer}实现
     *
     * @param <T> 超类类型
     */
    public abstract static class ForImplInstance<T> extends SuperTypeDeclarer {
        /**
         * 构建使用接口实现去实现超类接口的{@link SuperTypeDeclarer}实现
         *
         * @param superType    超类类型
         * @param implInstance 超类实现
         * @param <T>          超类泛型
         * @return SuperTypeDeclarer实例
         */
        public static <T> SuperTypeDeclarer build(Class<T> superType, T implInstance) {
            if (superType == null || !superType.isInterface() || implInstance == null) {
                throw new IllegalArgumentException(
                        "Super type has to be an interface and its implement instance should not be null. ");
            }
            return new ForImplInstance<T>() {
                @Override
                public Class<T> getSuperType() {
                    return superType;
                }

                @Override
                public T getImplInstance() {
                    return implInstance;
                }
            };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public abstract Class<T> getSuperType();

        @Override
        public DynamicType.Builder<?> resolve(Class<?> superType, DynamicType.Builder<?> builder) {
            return builder.implement(superType).method(ElementMatchers.isDeclaredBy(superType))
                    .intercept(MethodDelegation.withDefaultConfiguration().to(new ImplDelegator(getImplInstance())));
        }

        /**
         * 获取超类的实例
         *
         * @return 超类实例
         */
        protected abstract T getImplInstance();
    }

    /**
     * 依{@link BeanPropertyFlag#value()}值生成字段和get、set方法的{@link SuperTypeDeclarer}实现
     */
    public abstract static class ForBeanProperty extends SuperTypeDeclarer {
        /**
         * 构建依{@link BeanPropertyFlag#value()}值生成字段和get、set方法的{@link SuperTypeDeclarer}实现
         *
         * @param superType 超类类型
         * @return SuperTypeDeclarer实现
         */
        public static SuperTypeDeclarer build(Class<?> superType) {
            if (superType == null || !superType.isInterface()
                    || superType.getAnnotationsByType(BeanPropertyFlag.class).length <= 0) {
                throw new IllegalArgumentException(String.format(
                        "Super type has to be an interface annotated by [%s]. ", BeanPropertyFlag.class.getName()
                ));
            }
            return new ForBeanProperty() {
                @Override
                public Class<?> getSuperType() {
                    return superType;
                }
            };
        }

        @Override
        public DynamicType.Builder<?> resolve(Class<?> superType, DynamicType.Builder<?> builder) {
            final BeanPropertyFlag[] flags = superType.getAnnotationsByType(BeanPropertyFlag.class);
            if (flags.length <= 0) {
                return builder;
            }
            DynamicType.Builder<?> newBuilder = builder;
            for (BeanPropertyFlag beanPropertyFlag : flags) {
                newBuilder = newBuilder.defineField(beanPropertyFlag.value(), beanPropertyFlag.type(),
                        Visibility.PRIVATE);
            }
            return newBuilder.implement(superType).intercept(FieldAccessor.ofBeanProperty());
        }
    }
}
