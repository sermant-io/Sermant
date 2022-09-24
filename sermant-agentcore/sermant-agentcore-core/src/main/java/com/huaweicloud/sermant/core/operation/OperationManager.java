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

package com.huaweicloud.sermant.core.operation;

import com.huaweicloud.sermant.core.classloader.ClassLoaderManager;
import com.huaweicloud.sermant.core.exception.DupServiceException;
import com.huaweicloud.sermant.core.utils.SpiLoadUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 操作管理器
 *
 * @author luanwenfei
 * @since 2022-06-28
 */
public class OperationManager {
    private static final Map<String, BaseOperation> OPERATIONS = new HashMap<>();

    private OperationManager() {
    }

    /**
     * Init operations.
     */
    public static void initOperations() {
        for (final BaseOperation operation : ServiceLoader.load(BaseOperation.class,
            ClassLoaderManager.getFrameworkClassLoader())) {
            loadOperation(operation, operation.getClass(), BaseOperation.class);
        }
    }

    /**
     * 获取操作实例
     *
     * @param operationClass 操作class
     * @param <T> 服务泛型
     * @return operation 操作类实例
     * @throws IllegalArgumentException IllegalArgumentException
     */
    public static <T extends BaseOperation> T getOperation(Class<T> operationClass) {
        final BaseOperation baseOperation = OPERATIONS.get(operationClass.getName());
        if (baseOperation != null && operationClass.isAssignableFrom(baseOperation.getClass())) {
            return (T)baseOperation;
        }
        throw new IllegalArgumentException("Operation instance of [" + operationClass + "] is not found. ");
    }

    /**
     * 加载操作类实例至操作实例集
     *
     * @param operation 操作实例
     * @param operationCls 操作class
     * @param baseCls 基本操作class
     * @return 是否加载成功
     */
    private static boolean loadOperation(BaseOperation operation, Class<?> operationCls,
        Class<? extends BaseOperation> baseCls) {
        if (operationCls == null || operationCls == baseCls || !baseCls.isAssignableFrom(operationCls)) {
            return false;
        }
        final String operationName = operationCls.getName();
        final BaseOperation oldOperation = OPERATIONS.get(operationName);
        if (oldOperation != null && oldOperation.getClass() == operation.getClass()) {
            return false;
        }
        boolean isLoadSucceed = false;
        final BaseOperation betterOperation = SpiLoadUtils.getBetter(oldOperation, operation, (source, target) -> {
            throw new DupServiceException(operationName);
        });
        if (betterOperation != oldOperation) {
            OPERATIONS.put(operationName, operation);
            isLoadSucceed = true;
        }
        isLoadSucceed |= loadOperation(operation, operationCls.getSuperclass(), baseCls);
        for (Class<?> interfaceCls : operationCls.getInterfaces()) {
            isLoadSucceed |= loadOperation(operation, interfaceCls, baseCls);
        }
        return isLoadSucceed;
    }
}
