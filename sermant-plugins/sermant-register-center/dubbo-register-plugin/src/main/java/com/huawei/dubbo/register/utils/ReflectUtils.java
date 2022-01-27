/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.dubbo.register.utils;

import com.huawei.dubbo.register.cache.DubboCache;
import com.huawei.dubbo.register.constants.Constant;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 反射工具类，为了同时兼容alibaba和apache dubbo，所以需要用反射的方法进行类的操作
 *
 * @author provenceee
 * @date 2022/2/7
 */
public class ReflectUtils {
    private static final Logger LOGGER = LogFactory.getLogger();
    private static final String SC_REGISTRY_ADDRESS =
            Constant.SC_REGISTRY_PROTOCOL + Constant.PROTOCOL_SEPARATION + "127.0.0.1:30100";
    private static final String GET_PROTOCOL_METHOD_NAME = "getProtocol";
    private static final String GET_ADDRESS_METHOD_NAME = "getAddress";
    private static final String GET_PATH_METHOD_NAME = "getPath";
    private static final String GET_ID_METHOD_NAME = "getId";
    private static final String GET_NAME_METHOD_NAME = "getName";
    private static final String GET_PARAMETERS_METHOD_NAME = "getParameters";
    private static final String GET_REGISTRIES_METHOD_NAME = "getRegistries";
    private static final String IS_VALID_METHOD_NAME = "isValid";
    private static final String SET_HOST_METHOD_NAME = "setHost";
    private static final String SET_ADDRESS_METHOD_NAME = "setAddress";
    private static final String SET_ID_METHOD_NAME = "setId";
    private static final String SET_PREFIX_METHOD_NAME = "setPrefix";
    private static final String SET_PARAMETERS_METHOD_NAME = "setParameters";
    private static final String NOTIFY_METHOD_NAME = "notify";
    private static final String VALUE_OF_METHOD_NAME = "valueOf";

    /**
     * 新建注册配置
     *
     * @param clazz RegistryConfig(apache/alibaba)
     * @param <T> RegistryConfig(apache/alibaba)
     * @return 注册配置
     * @see com.alibaba.dubbo.config.RegistryConfig
     * @see org.apache.dubbo.config.RegistryConfig
     */
    public static <T> T newRegistryConfig(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor(String.class);
            // 这个url不重要，重要的是protocol，所以设置成127.0.0.1:30100就行
            return constructor.newInstance(SC_REGISTRY_ADDRESS);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot new the registryConfig.", e);
            return null;
        }
    }

    /**
     * 获取协议
     *
     * @param obj RegistryConfig | URL
     * @return 协议
     * @see com.alibaba.dubbo.config.RegistryConfig
     * @see org.apache.dubbo.config.RegistryConfig
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getProtocol(Object obj) {
        return invokeWithNoneParameterAndReturnString(obj, GET_PROTOCOL_METHOD_NAME);
    }

    /**
     * 获取应用地址
     *
     * @param obj URL
     * @return 应用地址
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getAddress(Object obj) {
        return invokeWithNoneParameterAndReturnString(obj, GET_ADDRESS_METHOD_NAME);
    }

    /**
     * 获取接口
     *
     * @param obj URL
     * @return 接口
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getPath(Object obj) {
        return invokeWithNoneParameterAndReturnString(obj, GET_PATH_METHOD_NAME);
    }

    /**
     * 获取注册id
     *
     * @param obj RegistryConfig
     * @return 注册id
     * @see com.alibaba.dubbo.config.RegistryConfig
     * @see org.apache.dubbo.config.RegistryConfig
     */
    public static String getId(Object obj) {
        return invokeWithNoneParameterAndReturnString(obj, GET_ID_METHOD_NAME);
    }

    /**
     * 获取dubbo应用名
     *
     * @param obj ApplicationConfig
     * @return 应用名
     * @see com.alibaba.dubbo.config.ApplicationConfig
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    public static String getName(Object obj) {
        return invokeWithNoneParameterAndReturnString(obj, GET_NAME_METHOD_NAME);
    }

    /**
     * 获取应用参数
     *
     * @param obj ApplicationConfig
     * @return 应用参数
     * @see com.alibaba.dubbo.config.ApplicationConfig
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    public static Map<String, String> getParameters(Object obj) {
        return invokeWithNoneParameter(obj, GET_PARAMETERS_METHOD_NAME, Map.class);
    }

    /**
     * 获取注册信息列表
     *
     * @param obj AbstractInterfaceConfig
     * @return 注册信息列表
     * @see com.alibaba.dubbo.config.AbstractInterfaceConfig
     * @see org.apache.dubbo.config.AbstractInterfaceConfig
     */
    public static List<Object> getRegistries(Object obj) {
        return invokeWithNoneParameter(obj, GET_REGISTRIES_METHOD_NAME, List.class);
    }

    /**
     * 判断注册信息是否有效
     *
     * @param obj RegistryConfig
     * @return 是否有效
     * @see com.alibaba.dubbo.config.RegistryConfig
     * @see org.apache.dubbo.config.RegistryConfig
     */
    public static boolean isValid(Object obj) {
        Boolean isValid = invokeWithNoneParameter(obj, IS_VALID_METHOD_NAME, Boolean.class);
        if (isValid == null) {
            // 为null代表没有这个方法，返回true
            return true;
        }
        return isValid;
    }

    /**
     * 设置host
     *
     * @param obj URL
     * @param host host
     * @return URL
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static Object setHost(Object obj, String host) {
        return invokeWithStringParameter(obj, SET_HOST_METHOD_NAME, host);
    }

    /**
     * 设置地址
     *
     * @param obj URL
     * @param address 地址
     * @return URL
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static Object setAddress(Object obj, String address) {
        return invokeWithStringParameter(obj, SET_ADDRESS_METHOD_NAME, address);
    }

    /**
     * 设置id
     *
     * @param obj RegistryConfig
     * @param id id
     * @see com.alibaba.dubbo.config.RegistryConfig
     * @see org.apache.dubbo.config.RegistryConfig
     */
    public static void setId(Object obj, String id) {
        invokeWithStringParameter(obj, SET_ID_METHOD_NAME, id);
    }

    /**
     * 设置前缀
     *
     * @param obj RegistryConfig
     * @param prefix 前缀
     * @see com.alibaba.dubbo.config.RegistryConfig
     * @see org.apache.dubbo.config.RegistryConfig
     */
    public static void setPrefix(Object obj, String prefix) {
        invokeWithStringParameter(obj, SET_PREFIX_METHOD_NAME, prefix);
    }

    /**
     * 设置注册时的参数
     *
     * @param obj ApplicationConfig
     * @param parameter 注册参数
     * @see com.alibaba.dubbo.config.ApplicationConfig
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    public static void setParameters(Object obj, Map<String, String> parameter) {
        invokeWithParameter(obj, SET_PARAMETERS_METHOD_NAME, parameter, Map.class);
    }

    /**
     * 通知dubbo下游接口的URL
     *
     * @param notifyListener 通知监听器
     * @param urls 下游接口的URL
     * @see com.alibaba.dubbo.registry.NotifyListener
     * @see org.apache.dubbo.registry.NotifyListener
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static void notify(Object notifyListener, List<Object> urls) {
        invokeWithParameter(notifyListener, NOTIFY_METHOD_NAME, urls, List.class);
    }

    /**
     * 根据address新建URL
     *
     * @param address 地址
     * @return URL
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static Object valueOf(String address) {
        return invoke(DubboCache.INSTANCE.getUrlClass(), null, VALUE_OF_METHOD_NAME, address, String.class);
    }

    private static String invokeWithNoneParameterAndReturnString(Object obj, String name) {
        return invokeWithNoneParameter(obj, name, String.class);
    }

    private static <T> T invokeWithNoneParameter(Object obj, String name, Class<T> returnClass) {
        return returnClass.cast(invoke(obj.getClass(), obj, name, null, null));
    }

    private static Object invokeWithStringParameter(Object obj, String name, String parameter) {
        return invokeWithParameter(obj, name, parameter, String.class);
    }

    private static Object invokeWithParameter(Object obj, String name, Object parameter, Class<?> parameterClass) {
        return invoke(obj.getClass(), obj, name, parameter, parameterClass);
    }

    private static Object invoke(Class<?> invokeClass, Object obj, String name, Object parameter,
            Class<?> parameterClass) {
        try {
            if (parameter == null || parameterClass == null) {
                return invokeClass.getMethod(name).invoke(obj);
            }
            return invokeClass.getMethod(name, parameterClass).invoke(obj, parameter);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // 因版本的原因，有可能会找不到方法，所以可以忽略这些错误
            return null;
        }
    }
}