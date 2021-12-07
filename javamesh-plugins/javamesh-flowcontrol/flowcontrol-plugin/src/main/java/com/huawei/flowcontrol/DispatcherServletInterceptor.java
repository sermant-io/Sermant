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
import com.huawei.flowcontrol.config.CommonConst;
import com.huawei.flowcontrol.entry.EntryFacade;
import com.huawei.flowcontrol.util.FilterUtil;
import com.huawei.flowcontrol.util.SentinelRuleUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * DispatcherServlet 的 API接口增强
 * 埋点定义sentinel资源
 *
 * @author liyi
 * @since 2020-08-26
 */
public class DispatcherServletInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] allArguments, BeforeResult result) throws Exception {
        HttpServletRequest req;
        if (allArguments[0] instanceof HttpServletRequest) {
            req = (HttpServletRequest) allArguments[0];
        } else {
            return;
        }
        try {
            EntryFacade.INSTANCE.tryEntry(req);
        } catch (BlockException ex) {
            RecordLog.info("[DispatcherServletInterceptor] has been blocked! "
                    + "appName= " + ex.getRuleLimitApp() + " resourceName=" + FilterUtil.filterTarget(req));
            HttpServletResponse resp = null;
            if (allArguments[1] instanceof HttpServletResponse) {
                resp = (HttpServletResponse) allArguments[1];
            }
            String errMsg = SentinelRuleUtil.getResult(ex.getRule());
            if (resp != null) {
                resp.setStatus(CommonConst.HTTP_STATUS_429);
                resp.getWriter().write(errMsg);
            }
            // 不再执行业务代码
            result.setResult(errMsg);
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] allArguments, Object ret) {
        EntryFacade.INSTANCE.exit();
        return ret;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] allArguments, Throwable t) {
        if (t != null) {
            EntryFacade.INSTANCE.tryTraceEntry(t);
            EntryFacade.INSTANCE.exit();
            RecordLog.error("[DispatcherServletInterceptor] exception：" + t.getMessage());
        }
    }
}
