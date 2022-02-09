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

package com.huawei.flowcontrol.service;

import com.huawei.flowcontrol.config.CommonConst;
import com.huawei.flowcontrol.entry.EntryFacade;
import com.huawei.flowcontrol.util.FilterUtil;
import com.huawei.flowcontrol.util.SentinelRuleUtil;
import com.huawei.sermant.core.agent.common.BeforeResult;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * http请求拦截实现
 *
 * @author zhouss
 * @since 2021-12-27
 */
public class DispatcherServletServiceImpl extends DispatcherServletService {
    /**
     * 拦截点前执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param beforeResult 执行结果承载类
     */
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        HttpServletRequest req;
        if (arguments[0] instanceof HttpServletRequest) {
            req = (HttpServletRequest) arguments[0];
        } else {
            return;
        }
        try {
            EntryFacade.INSTANCE.tryEntry(req);
        } catch (BlockException ex) {
            RecordLog.info("[DispatcherServletInterceptor] has been blocked! "
                    + "appName= " + ex.getRuleLimitApp() + " resourceName=" + FilterUtil.filterTarget(req));
            HttpServletResponse resp = null;
            if (arguments[1] instanceof HttpServletResponse) {
                resp = (HttpServletResponse) arguments[1];
            }
            String errMsg = SentinelRuleUtil.getResult(ex.getRule());
            if (resp != null) {
                resp.setStatus(CommonConst.HTTP_STATUS_429);
                resp.getWriter().write(errMsg);
            }
            // 不再执行业务代码
            beforeResult.setResult(errMsg);
        }
    }

    /**
     * 拦截点之后执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param result the method's original return value. May be null if the method triggers an exception.
     */
    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) {
        EntryFacade.INSTANCE.exit();
    }

    /**
     * 拦截点之后执行
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param throwable 增强时可能出现的异常
     */
    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        if (throwable != null) {
            EntryFacade.INSTANCE.tryTraceEntry(throwable);
            EntryFacade.INSTANCE.exit();
            RecordLog.error("[DispatcherServletInterceptor] exception：" + throwable.getMessage());
        }
    }
}
