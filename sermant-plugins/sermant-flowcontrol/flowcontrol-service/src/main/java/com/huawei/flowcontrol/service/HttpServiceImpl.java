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

import com.huawei.flowcontrol.common.entity.FixedResult;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.entry.EntryFacade;
import com.huawei.flowcontrol.service.sen.HttpSenService;
import com.huawei.flowcontrol.util.SentinelRuleUtil;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * http sentinel拦截实现
 *
 * @author zhouss
 * @since 2022-01-25
 */
public class HttpServiceImpl extends HttpSenService {
    @Override
    public void onBefore(RequestEntity requestEntity, FixedResult fixedResult) {
        try {
            EntryFacade.INSTANCE.tryEntry(requestEntity);
        } catch (BlockException ex) {
            SentinelRuleUtil.handleBlockException(ex, fixedResult);
            EntryFacade.INSTANCE.exit();
        }
    }

    @Override
    public void onAfter(Object result) {
        EntryFacade.INSTANCE.exit();
    }

    @Override
    public void onThrow(Throwable throwable) {
        if (throwable != null) {
            EntryFacade.INSTANCE.tryTraceEntry(throwable);
            EntryFacade.INSTANCE.exit();
            RecordLog.error("[HttpServiceImpl] exception：" + throwable.getMessage());
        }
    }
}
