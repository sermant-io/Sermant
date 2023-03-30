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

package com.huaweicloud.sermant.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * SpringBoot服务调用增强类
 *
 * @author zhp
 * @since 2023-02-17
 */
public class SpringBootApplyInterceptor extends AbstractCallInterceptor<Object> {
    private static final String INSTANCE_NAME = "serviceInstance";

    private static final String HOST_NAME = "host";

    private static final String PORT_NAME = "port";

    private static final String EXCEPTION_NAME = "ex";

    @Override
    protected String getHost(Object object) {
        return getFiledValue(HOST_NAME, object);
    }

    @Override
    protected String getPort(Object object) {
        return getFiledValue(PORT_NAME, object);
    }

    /**
     * 获取实例信息的参数下标
     *
     * @return 例信息的参数下标
     */
    protected int getIndex() {
        return 1;
    }

    /**
     * 获取字段值
     *
     * @param fieldName 字段名称
     * @param object 服务实例信息
     * @return 字段的值
     */
    private String getFiledValue(String fieldName, Object object) {
        Optional<Object> instanceOptional = ReflectUtils.getFieldValue(object, INSTANCE_NAME);
        if (!instanceOptional.isPresent()) {
            return StringUtils.EMPTY;
        }
        Optional<Object> hostOptional = ReflectUtils.getFieldValue(instanceOptional.get(), fieldName);
        if (hostOptional.isPresent()) {
            return StringUtils.getString(hostOptional.get());
        }
        return StringUtils.EMPTY;
    }

    /**
     * 判断调用结果
     *
     * @param context 上下文信息
     * @return 调用结果成功或者失败
     */
    protected boolean isSuccess(ExecuteContext context) {
        if (removalConfig.getExceptions() == null || removalConfig.getExceptions().isEmpty()) {
            return true;
        }
        List<String> exceptions = removalConfig.getExceptions();
        Optional<Object> exOptional = ReflectUtils.getFieldValue(context.getArguments()[1], EXCEPTION_NAME);
        if (!exOptional.isPresent() || !(exOptional.get() instanceof Throwable)) {
            return true;
        }
        Throwable throwable = (Throwable) exOptional.get();
        if (throwable.getCause() == null) {
            return !exceptions.contains(throwable.getClass().getName());
        }
        return !exceptions.contains(throwable.getCause().getClass().getName());
    }
}
