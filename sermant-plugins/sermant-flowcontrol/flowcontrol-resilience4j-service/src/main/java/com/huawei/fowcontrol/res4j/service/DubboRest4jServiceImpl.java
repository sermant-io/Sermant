/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.fowcontrol.res4j.service;

import com.huawei.flowcontrol.common.entity.FixedResult;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.service.rest4j.DubboRest4jService;
import com.huawei.fowcontrol.res4j.handler.HandlerFacade;
import com.huawei.fowcontrol.res4j.util.Rest4jExceptionUtils;

/**
 * http请求拦截逻辑实现
 *
 * @author zhouss
 * @since 2022-01-25
 */
public class DubboRest4jServiceImpl extends DubboRest4jService {
    private final Exception dubboException = new Exception("dubbo exception");

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void onBefore(RequestEntity requestEntity, FixedResult fixedResult, boolean isProvider) {
        try {
            HandlerFacade.INSTANCE.injectHandlers(requestEntity, isProvider);
        } catch (Exception ex) {
            Rest4jExceptionUtils.handleException(ex, fixedResult);
            HandlerFacade.INSTANCE.removeHandlers(isProvider);
        }
    }

    @Override
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public void onAfter(Object result, boolean isProvider, boolean hasException) {
        HandlerFacade.INSTANCE.onDubboResult(result);
        if (hasException) {
            // 此处主要标记异常状态，具体异常信息与记录结果无关
            HandlerFacade.INSTANCE.onDubboThrow(dubboException);
        }
        HandlerFacade.INSTANCE.removeHandlers(isProvider);
    }

    @Override
    public boolean onThrow(Throwable throwable, boolean isProvider) {
        HandlerFacade.INSTANCE.onDubboThrow(throwable);
        HandlerFacade.INSTANCE.removeHandlers(isProvider);
        return false;
    }
}
