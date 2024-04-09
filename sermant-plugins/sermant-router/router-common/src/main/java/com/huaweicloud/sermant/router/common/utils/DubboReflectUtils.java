/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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
 * To be compatible with both Alibaba and Apache Dubbo, you need to use the reflection method to perform the class
 * operation
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
     * get the queryMap
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
     * Get the parameters
     *
     * @param obj url
     * @param key key
     * @return parameter
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getParameter(Object obj, String key) {
        return (String) ReflectUtils.invokeWithParameter(obj, GET_PARAMETER_METHOD_NAME, key, String.class);
    }

    /**
     * Get the application parameters
     *
     * @param obj ApplicationConfig
     * @return Apply parameters
     * @see com.alibaba.dubbo.config.ApplicationConfig
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    public static Map<String, String> getParameters(Object obj) {
        return (Map<String, String>) ReflectUtils.invokeWithNoneParameter(obj, GET_PARAMETERS_METHOD_NAME);
    }

    /**
     * Get the URL
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
     * Obtain the name of the service interface
     *
     * @param obj url
     * @return The name of the service interface
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getServiceInterface(Object obj) {
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, GET_SERVICE_INTERFACE_METHOD_NAME);
    }

    /**
     * Obtain the name of the service interface
     *
     * @param obj url
     * @return The name of the service interface
     * @see com.alibaba.dubbo.common.URL
     * @see org.apache.dubbo.common.URL
     */
    public static String getServiceKey(Object obj) {
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, GET_SERVICE_KEY_METHOD_NAME);
    }

    /**
     * Obtain the name of the dubbo request method
     *
     * @param obj invocation
     * @return The name of the dubbo request method
     * @see com.alibaba.dubbo.rpc.Invocation
     * @see org.apache.dubbo.rpc.Invocation
     */
    public static String getMethodName(Object obj) {
        return ReflectUtils.invokeWithNoneParameterAndReturnString(obj, GET_METHOD_NAME_METHOD_NAME);
    }

    /**
     * Obtain the parameters of the dubbo request
     *
     * @param obj invocation
     * @return dubbo request parameter
     * @see com.alibaba.dubbo.rpc.Invocation
     * @see org.apache.dubbo.rpc.Invocation
     */
    public static Object[] getArguments(Object obj) {
        return (Object[]) ReflectUtils.invokeWithNoneParameter(obj, GET_ARGUMENTS_METHOD_NAME);
    }

    /**
     * Set the parameters at the time of registration
     *
     * @param obj ApplicationConfig
     * @param parameter Registration parameters
     * @see com.alibaba.dubbo.config.ApplicationConfig
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    public static void setParameters(Object obj, Map<String, String> parameter) {
        ReflectUtils.invokeWithParameter(obj, SET_PARAMETERS_METHOD_NAME, parameter, Map.class);
    }

    /**
     * Get dubbo request attachments parameter
     *
     * @param obj invocation
     * @return dubbo attachments parameter
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
     * Obtain the attachment parameter from dubbo Invocation
     *
     * @param obj invocation
     * @return dubbo attachments parameter
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
     * Get the ServiceInstance
     *
     * @param obj invoker
     * @return ServiceInstance
     * @see org.apache.dubbo.rpc.Invoker
     */
    public static Object getInstance(Object obj) {
        return ReflectUtils.invokeWithNoneParameter(obj, GET_INSTANCE_METHOD_NAME);
    }

    /**
     * Get the MetadataInfo
     *
     * @param obj invoker
     * @return MetadataInfo
     * @see org.apache.dubbo.rpc.Invoker
     */
    public static Object getMetadataInfo(Object obj) {
        return ReflectUtils.invokeWithNoneParameter(obj, GET_METADATA_INFO_METHOD_NAME);
    }

    /**
     * Get the serviceKey
     *
     * @param obj invoker
     * @return String
     * @see org.apache.dubbo.rpc.Invoker
     */
    public static Object getProtocolServiceKey(Object obj) {
        return ReflectUtils.invokeWithNoneParameter(obj, GET_PROTOCOL_SERVICE_METHOD_NAME);
    }
}