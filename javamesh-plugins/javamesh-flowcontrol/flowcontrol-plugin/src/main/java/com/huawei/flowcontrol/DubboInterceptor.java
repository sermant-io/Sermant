/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowcontrol;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.flowcontrol.exception.FlowControlException;
import com.huawei.flowcontrol.entry.EntryFacade;
import com.huawei.flowcontrol.util.SentinelRuleUtil;

import java.util.Locale;

public abstract class DubboInterceptor implements InstanceMethodInterceptor {

    protected String getResourceName(String interfaceName, String methodName) {
        return interfaceName + ":" + methodName;
    }

    protected void handleBlockException(BlockException ex, String resourceName, BeforeResult result, String type, EntryFacade.DubboType dubboType) {
        try {
            final String msg = String.format(Locale.ENGLISH,
                "[%s] has been blocked! [appName=%s, resourceName=%s]",
                type, ex.getRuleLimitApp(), resourceName);
            RecordLog.info(msg);
            String res = SentinelRuleUtil.getResult(ex.getRule());
            result.setResult(res);
            throw new FlowControlException(res);
        } finally {
            if (EntryFacade.DubboType.APACHE == dubboType) {
                EntryFacade.INSTANCE.exit(dubboType);
            } else {
                EntryFacade.INSTANCE.exit(dubboType);
            }
        }
    }
}
