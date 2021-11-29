/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
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
            result.setResult(null);
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
