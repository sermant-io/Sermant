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

package com.huaweicloud.sermant.router.dubbo.interceptor;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.LogUtils;
import com.huaweicloud.sermant.router.common.service.AbstractDirectoryService;

import java.util.List;
import java.util.logging.Logger;

/**
 * 增强AbstractDirectory的子类的doList方法，筛选标签应用的地址
 *
 * @author provenceee
 * @since 2021-06-28
 */
public class AbstractDirectoryInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int LENGTH_ONE = 1;

    private static final int LENGTH_TWO = 2;

    private static final int LENGTH_THREE = 3;

    private final AbstractDirectoryService abstractDirectoryService;

    /**
     * 构造方法
     */
    public AbstractDirectoryInterceptor() {
        abstractDirectoryService = PluginServiceManager.getPluginService(AbstractDirectoryService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        LogUtils.printDubboRequestBeforePoint(context);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        Object[] arguments = context.getArguments();

        // DUBBO 2.x and DUBBO 3.O.x dolist method is one parameter
        // DUBBO 3.1.x dolist method is two parameter
        // DUBBO 3.2.x dolist method is three parameter
        Object invocation = arguments.length == LENGTH_ONE ? arguments[LENGTH_ONE - 1]
                : arguments.length == LENGTH_TWO ? arguments[LENGTH_TWO - 1]
                : arguments.length == LENGTH_THREE ? arguments[LENGTH_THREE - 1] : null;
        LOGGER.info("======arguments length==========" + arguments.length
                + "=========serverList size============" + ((List<Object>) context.getResult()).size());
        context.changeResult(abstractDirectoryService.selectInvokers(context.getObject(), invocation,
                context.getResult()));
        LogUtils.printDubboRequestAfterPoint(context);
        return context;
    }
}