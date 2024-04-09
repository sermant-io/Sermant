/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.util;

import com.huawei.flowcontrol.common.config.CommonConst;

/**
 * conversion tool class
 *
 * @author zhouss
 * @since 2022-01-22
 */
public class ConvertUtils {
    /**
     * obtain the version version from dubbo attachment
     */
    public static final String DUBBO_ATTACHMENT_VERSION = "version";

    /**
     * dubbo default default version
     */
    public static final String ABSENT_VERSION = "0.0.0";

    private ConvertUtils() {
    }

    /**
     * Whether the interface is a generalization interface
     * <p></p>
     * {@link org.apache.dubbo.rpc.service.GenericService}、 {@link com.alibaba.dubbo.rpc.service.GenericService} To make
     * a call using a wrapper proxy, need to compare the class fully qualified name with the method name
     *
     * @param interfaceName interface name
     * @param methodName method name
     * @return Whether the interface is a generalization interface
     */
    public static boolean isGenericService(String interfaceName, String methodName) {
        return (CommonConst.ALIBABA_DUBBO_GENERIC_SERVICE_CLASS.equals(interfaceName)
                || CommonConst.APACHE_DUBBO_GENERIC_SERVICE_CLASS.equals(interfaceName))
                && CommonConst.GENERIC_METHOD_NAME.equals(methodName);
    }

    /**
     * buildAPIPath
     *
     * @param interfaceName interface name
     * @param version version
     * @param methodName method name
     * @return api path
     */
    public static String buildApiPath(String interfaceName, String version, String methodName) {
        if (version == null || "".equals(version) || ABSENT_VERSION.equals(version)) {
            return interfaceName + "." + methodName;
        }

        // com.huawei.dubbotest.service.CTest:version.methodName
        return interfaceName + ":" + version + "." + methodName;
    }
}
