/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huawei.flowcontrol.service.DispatcherServletService;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.service.ServiceManager;

import java.lang.reflect.Method;

/**
 * DispatcherServlet 的 API接口增强
 * 埋点定义sentinel资源
 *
 * @author liyi
 * @since 2020-08-26
 */
public class DispatcherServletInterceptor implements InstanceMethodInterceptor {
    private DispatcherServletService dispatcherServletService;

    @Override
    public void before(Object obj, Method method, Object[] allArguments, BeforeResult result) throws Exception {
        dispatcherServletService = ServiceManager.getService(DispatcherServletService.class);
        dispatcherServletService.before(obj, method, allArguments, result);
//        HttpServletRequest req;
//        if (allArguments[0] instanceof HttpServletRequest) {
//            req = (HttpServletRequest) allArguments[0];
//        } else {
//            return;
//        }
//        try {
//            EntryFacade.INSTANCE.tryEntry(req);
//        } catch (BlockException ex) {
//            RecordLog.info("[DispatcherServletInterceptor] has been blocked! "
//                    + "appName= " + ex.getRuleLimitApp() + " resourceName=" + FilterUtil.filterTarget(req));
//            HttpServletResponse resp = null;
//            if (allArguments[1] instanceof HttpServletResponse) {
//                resp = (HttpServletResponse) allArguments[1];
//            }
//            String errMsg = SentinelRuleUtil.getResult(ex.getRule());
//            if (resp != null) {
//                resp.setStatus(CommonConst.HTTP_STATUS_429);
//                resp.getWriter().write(errMsg);
//            }
//            // 不再执行业务代码
//            result.setResult(errMsg);
//        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] allArguments, Object ret) {
        dispatcherServletService.after(obj, method, allArguments, ret);
//        EntryFacade.INSTANCE.exit();
        return ret;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] allArguments, Throwable t) {
        dispatcherServletService.onThrow(obj, method, allArguments, t);
//        if (t != null) {
//            EntryFacade.INSTANCE.tryTraceEntry(t);
//            EntryFacade.INSTANCE.exit();
//            RecordLog.error("[DispatcherServletInterceptor] exception：" + t.getMessage());
//        }
    }
}
