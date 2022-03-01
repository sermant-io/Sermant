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

package com.huawei.flowcontrol.service;

import com.huawei.flowcontrol.common.entity.FlowControlResult;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.entry.EntryFacade;
import com.huawei.flowcontrol.service.sen.DubboSenService;
import com.huawei.flowcontrol.util.SentinelRuleUtil;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * dubbo 基于sentinel拦截
 *
 * @author zhouss
 * @since 2022-01-25
 */
public class DubboServiceImpl extends DubboSenService {
    private final Exception dubboException = new Exception("dubbo exception");

    @Override
    public void onBefore(RequestEntity requestEntity, FlowControlResult fixedResult, boolean isProvider) {
        try {
            EntryFacade.INSTANCE.tryEntry(requestEntity, isProvider);
        } catch (BlockException blockException) {
            SentinelRuleUtil.handleBlockException(blockException, fixedResult);
            EntryFacade.INSTANCE.exitDubbo();
        }
    }

    @Override
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public void onAfter(Object result, boolean isProvider, boolean hasException) {
        if (hasException) {
            // 此处主要标记异常状态，具体异常信息与记录结果无关
            EntryFacade.INSTANCE.tryTraceEntry(dubboException, isProvider);
        }
        EntryFacade.INSTANCE.exitDubbo();
    }

    @Override
    public boolean onThrow(Throwable throwable, boolean isProvider) {
        if (throwable != null) {
            EntryFacade.INSTANCE
                .tryTraceEntry(throwable, isProvider);
        }
        EntryFacade.INSTANCE.exitDubbo();
        return false;
    }
}
