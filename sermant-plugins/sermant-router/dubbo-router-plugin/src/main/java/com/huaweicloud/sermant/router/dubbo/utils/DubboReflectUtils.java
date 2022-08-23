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

package com.huaweicloud.sermant.router.dubbo.utils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.ReflectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 反射工具类，为了同时兼容alibaba和apache dubbo，所以需要用反射的方法进行类的操作
 *
 * @author provenceee
 * @since 2022-02-07
 */
public class DubboReflectUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final String QUERY_MAP_FIELD_NAME = "queryMap";
    private static final String GET_ADDRESS_METHOD_NAME = "getAddress";
    private static final String GET_NAME_METHOD_NAME = "getName";
    private static final String GET_PARAMETER_METHOD_NAME = "getParameter";
    private static final String GET_PARAMETERS_METHOD_NAME = "getParameters";
    private static final String GET_URL_METHOD_NAME = "getUrl";
    private static final String GET_SERVICE_INTERFACE_METHOD_NAME = "getServiceInterface";
    private static final String GET_METHOD_NAME_METHOD_NAME = "getMethodName";
    private static final String GET_ARGUMENTS_METHOD_NAME = "getArguments";
    private static final String SET_PARAMETERS_METHOD_NAME = "setParameters";
    private static final String GET_ATTACHMENTS_METHOD_NAME = "getAttachments";
    private static final String GET_OBJECT_ATTACHMENTS_METHOD_NAME = "getObjectAttachments";
    private static final String GET_CONTEXT_METHOD_NAME = "getContext";
    private static final String ALIBABA_RPC_CONTEXT_CLASS_NAME = "com.alibaba.dubbo.rpc.RpcContext";

    private DubboReflectUtils() {
    }

    /**
     * 获取queryMap
     *
     * @param obj RegistryDirectory
     * @return queryMap
     * @see com.alibaba.dubbo.registry.integration.RegistryDirectory
     * @see org.apache.dubbo.registry.integration.RegistryDirectory
     */
    public static Map<String, String> getQueryMap(Object obj) {
        return (Map<String, String>) ReflectUtils.getFieldValue(obj, QUERY_MAP_FIELD_NAME).orElse(null);
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
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, GET_ADDRESS_METHOD_NAME);
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
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, GET_NAME_METHOD_NAME);
    }

    /**
     * 获取参数
     *
     * @param obj url
     * @param key 键
     * @param defaultValue 默认值
     * @return 参数
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getParameter(Object obj, String key, String defaultValue) {
        String value = getParameter(obj, key);
        return value == null ? defaultValue : value;
    }

    /**
     * 获取参数
     *
     * @param obj url
     * @param key 键
     * @return 参数
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getParameter(Object obj, String key) {
        return (String) ReflectUtils.invokeWithParameter(obj, GET_PARAMETER_METHOD_NAME, key, String.class);
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
        return (Map<String, String>) ReflectUtils.invokeWithNoneParameter(obj, GET_PARAMETERS_METHOD_NAME);
    }

    /**
     * 获取url
     *
     * @param obj invoker
     * @return url
     * @see com.alibaba.dubbo.rpc.Invoker
     * @see org.apache.dubbo.rpc.Invoker
     */
    public static Object getUrl(Object obj) {
        return ReflectUtils.invokeWithNoneParameter(obj, GET_URL_METHOD_NAME);
    }

    /**
     * 获取服务接口名
     *
     * @param obj url
     * @return 服务接口名
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getServiceInterface(Object obj) {
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, GET_SERVICE_INTERFACE_METHOD_NAME);
    }

    /**
     * 获取dubbo请求方法名
     *
     * @param obj invocation
     * @return dubbo请求方法名
     * @see com.alibaba.dubbo.rpc.Invocation
     * @see org.apache.dubbo.rpc.Invocation
     */
    public static String getMethodName(Object obj) {
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, GET_METHOD_NAME_METHOD_NAME);
    }

    /**
     * 获取dubbo请求参数
     *
     * @param obj invocation
     * @return dubbo请求参数
     * @see com.alibaba.dubbo.rpc.Invocation
     * @see org.apache.dubbo.rpc.Invocation
     */
    public static Object[] getArguments(Object obj) {
        return (Object[]) ReflectUtils.invokeWithNoneParameter(obj, GET_ARGUMENTS_METHOD_NAME);
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
        ReflectUtils.invokeWithParameter(obj, SET_PARAMETERS_METHOD_NAME, parameter, Map.class);
    }

    /**
     * 获取dubbo请求attachments参数
     *
     * @param obj invocation
     * @return dubbo attachments参数
     */
    public static Map<String, String> getAttachments(Object obj) {
        Map<String, String> attachments = ReflectUtils.invokeWithNoneParameterAndReturnMap(
                obj, GET_ATTACHMENTS_METHOD_NAME);
        if (!CollectionUtils.isEmpty(attachments)) {
            return attachments;
        }
        Map<String, String> objectAttachments = ReflectUtils.invokeWithNoneParameterAndReturnMap(
                obj, GET_OBJECT_ATTACHMENTS_METHOD_NAME);
        if (!CollectionUtils.isEmpty(objectAttachments)) {
            return objectAttachments;
        }
        try {
            Class<?> rpcContextClazz = Class.forName(ALIBABA_RPC_CONTEXT_CLASS_NAME);
            Method getContextMethod = rpcContextClazz.getDeclaredMethod(GET_CONTEXT_METHOD_NAME);
            return ReflectUtils.invokeWithNoneParameterAndReturnMap(
                    getContextMethod.invoke(rpcContextClazz), GET_ATTACHMENTS_METHOD_NAME);
        } catch (NoSuchMethodException | ClassNotFoundException
                | InvocationTargetException | IllegalAccessException e) {
            // 因版本原因，可能会找不到方法
            LOGGER.warning(e.getMessage());
        }
        return Collections.emptyMap();
    }
}