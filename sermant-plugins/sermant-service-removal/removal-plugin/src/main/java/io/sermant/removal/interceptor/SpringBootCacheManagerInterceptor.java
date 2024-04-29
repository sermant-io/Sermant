/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.removal.interceptor;

import io.sermant.core.utils.ReflectUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.removal.common.RemovalConstants;

import java.util.Optional;

/**
 * SpringBoot service call enhancement class
 *
 * @author zhp
 * @since 2023-02-17
 */
public class SpringBootCacheManagerInterceptor extends AbstractRemovalInterceptor<Object> {
    private static final String HOST_FILED_NAME = "host";

    private static final String SERVICE_NAME_FILED_NAME = "serviceName";

    private static final String PORT_FILED_NAME = "port";

    @Override
    protected String createKey(Object instance) {
        Optional<Object> hostOptional = ReflectUtils.getFieldValue(instance, HOST_FILED_NAME);
        Optional<Object> portOptional = ReflectUtils.getFieldValue(instance, PORT_FILED_NAME);
        if (hostOptional.isPresent() && portOptional.isPresent()) {
            return StringUtils.getString(hostOptional.get()) + RemovalConstants.CONNECTOR
                    + StringUtils.getString(portOptional.get());
        }
        return StringUtils.EMPTY;
    }

    @Override
    protected String getServiceKey(Object instance) {
        Optional<Object> serviceNameOptional = ReflectUtils.getFieldValue(instance, SERVICE_NAME_FILED_NAME);
        if (serviceNameOptional.isPresent()) {
            return StringUtils.getString(serviceNameOptional.get());
        }
        return StringUtils.EMPTY;
    }
}
