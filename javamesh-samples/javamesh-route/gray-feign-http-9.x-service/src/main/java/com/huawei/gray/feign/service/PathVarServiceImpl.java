/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.feign.service;

import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.gray.feign.context.FeignResolvedURL;

import feign.RequestTemplate;

import java.lang.reflect.Method;

/**
 * PathVarInterceptor的service
 *
 * @author pengyuyi
 * @date 2021/11/26
 */
public class PathVarServiceImpl implements PathVarService {
    static final ThreadLocal<FeignResolvedURL> URL_CONTEXT = new ThreadLocal<FeignResolvedURL>();

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        RequestTemplate template = (RequestTemplate) arguments[1];
        URL_CONTEXT.set(new FeignResolvedURL(template.url()));
    }

    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        RequestTemplate resolvedTemplate = (RequestTemplate) result;
        URL_CONTEXT.get().setUrl(resolvedTemplate.url());
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        if (URL_CONTEXT.get() != null) {
            URL_CONTEXT.remove();
        }
    }
}
