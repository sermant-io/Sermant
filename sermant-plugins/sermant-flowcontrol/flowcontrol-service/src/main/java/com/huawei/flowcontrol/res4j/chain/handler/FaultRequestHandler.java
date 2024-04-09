/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.res4j.chain.handler;

import com.huawei.flowcontrol.common.core.rule.fault.Fault;
import com.huawei.flowcontrol.common.entity.RequestEntity.RequestType;
import com.huawei.flowcontrol.res4j.chain.HandlerConstants;
import com.huawei.flowcontrol.res4j.chain.context.RequestContext;
import com.huawei.flowcontrol.res4j.handler.FaultHandler;

import java.util.List;
import java.util.Set;

/**
 * error injection request handler
 *
 * @author zhouss
 * @since 2022-08-08
 */
public class FaultRequestHandler extends FlowControlHandler<Fault> {
    private final FaultHandler faultHandler = new FaultHandler();

    private final String contextName = FaultRequestHandler.class.getName();

    @Override
    public void onBefore(RequestContext context, Set<String> businessNames) {
        final List<Fault> faults = faultHandler.createOrGetHandlers(businessNames);
        if (!faults.isEmpty()) {
            faults.forEach(Fault::acquirePermission);
            context.save(getContextName(), faults);
        }
        super.onBefore(context, businessNames);
    }

    @Override
    public void onResult(RequestContext context, Set<String> businessNames, Object result) {
        context.remove(getContextName());
        super.onResult(context, businessNames, result);
    }

    @Override
    public int getOrder() {
        return HandlerConstants.FAULT_ORDER;
    }

    @Override
    protected RequestType direct() {
        return RequestType.CLIENT;
    }

    @Override
    public String getContextName() {
        return contextName;
    }
}
