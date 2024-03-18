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

package com.huaweicloud.sermant.router.common.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 反射工具类，为了同时兼容alibaba和apache dubbo，所以需要用反射的方法进行类的操作
 *
 * @author provenceee
 * @since 2022-02-07
 */
public class DubboReflectUtils {
    private static final String QUERY_MAP_FIELD_NAME = "queryMap";

    private static final String GET_PARAMETER_METHOD_NAME = "getParameter";

    private static final String GET_PARAMETERS_METHOD_NAME = "getParameters";

    private static final String GET_URL_METHOD_NAME = "getUrl";

    private static final String GET_SERVICE_INTERFACE_METHOD_NAME = "getServiceInterface";

    private static final String GET_SERVICE_KEY_METHOD_NAME = "getServiceKey";

    private static final String GET_METHOD_NAME_METHOD_NAME = "getMethodName";

    private static final String GET_ARGUMENTS_METHOD_NAME = "getArguments";

    private static final String SET_PARAMETERS_METHOD_NAME = "setParameters";

    private static final String GET_CONTEXT_METHOD_NAME = "getContext";

    private static final String ALIBABA_RPC_CONTEXT_CLASS_NAME = "com.alibaba.dubbo.rpc.RpcContext";

    private static final String APACHE_RPC_CONTEXT_CLASS_NAME = "org.apache.dubbo.rpc.RpcContext";

    private static final String ATTACHMENTS_FIELD = "attachments";

    private static final String GET_INSTANCE_METHOD_NAME = "getInstance";

    private static final String GET_METADATA_INFO_METHOD_NAME = "getMetadataInfo";

    private static final String GET_PROTOCOL_SERVICE_METHOD_NAME = "getProtocolServiceKey";

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
     * 获取服务接口名
     *
     * @param obj url
     * @return 服务接口名
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getServiceKey(Object obj) {
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, GET_SERVICE_KEY_METHOD_NAME);
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
     * @see com.alibaba.dubbo.rpc.Invocation
     * @see org.apache.dubbo.rpc.Invocation
     * @see com.alibaba.dubbo.rpc.RpcContext
     * @see org.apache.dubbo.rpc.RpcContext
     */
    public static Map<String, Object> getAttachments(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        String className = obj.getClass().getName().startsWith("com.alibaba.dubbo")
                ? ALIBABA_RPC_CONTEXT_CLASS_NAME : APACHE_RPC_CONTEXT_CLASS_NAME;
        Map<String, Object> attachments = new HashMap<>(getAttachmentsFromContext(className));
        attachments.putAll(getAttachmentsByInvocation(obj));
        return Collections.unmodifiableMap(attachments);
    }

    /**
     * 获取dubbo Invocation中的attachments参数
     *
     * @param obj invocation
     * @return dubbo attachments参数
     * @see com.alibaba.dubbo.rpc.Invocation
     * @see org.apache.dubbo.rpc.Invocation
     */
    public static Map<String, Object> getAttachmentsByInvocation(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        return ReflectUtils.getFieldValue(obj, ATTACHMENTS_FIELD).map(map -> (Map<String, Object>) map)
                .orElse(Collections.emptyMap());
    }

    private static Map<String, Object> getAttachmentsFromContext(String contextClazz) {
        Optional<Object> context = com.huaweicloud.sermant.core.utils.ReflectUtils
                .invokeMethod(contextClazz, GET_CONTEXT_METHOD_NAME, null, null);
        if (!context.isPresent()) {
            return Collections.emptyMap();
        }
        Optional<Object> attachments = ReflectUtils.getFieldValue(context.get(), ATTACHMENTS_FIELD);
        return attachments.map(obj -> (Map<String, Object>) obj).orElse(Collections.emptyMap());
    }

    /**
     * 获取ServiceInstance
     *
     * @param obj invoker
     * @return ServiceInstance
     * @see org.apache.dubbo.rpc.Invoker
     */
    public static Object getInstance(Object obj) {
        return ReflectUtils.invokeWithNoneParameter(obj, GET_INSTANCE_METHOD_NAME);
    }

    /**
     * 获取MetadataInfo
     *
     * @param obj invoker
     * @return MetadataInfo
     * @see org.apache.dubbo.rpc.Invoker
     */
    public static Object getMetadataInfo(Object obj) {
        return ReflectUtils.invokeWithNoneParameter(obj, GET_METADATA_INFO_METHOD_NAME);
    }

    /**
     * 获取serviceKey
     *
     * @param obj invoker
     * @return String
     * @see org.apache.dubbo.rpc.Invoker
     */
    public static Object getProtocolServiceKey(Object obj) {
        return ReflectUtils.invokeWithNoneParameter(obj, GET_PROTOCOL_SERVICE_METHOD_NAME);
    }
}