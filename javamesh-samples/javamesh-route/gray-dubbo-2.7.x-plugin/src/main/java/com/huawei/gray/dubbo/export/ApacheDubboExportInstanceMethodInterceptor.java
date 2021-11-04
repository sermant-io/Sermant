/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.export;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.gray.dubbo.utils.RouterUtil;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.ServiceConfig;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 增强ServiceConfig类的export方法，用来获取消费者对外暴露的接口
 *
 * @author l30008180
 * @since 2021年7月7日
 */
public class ApacheDubboExportInstanceMethodInterceptor implements InstanceMethodInterceptor {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String URLS_FIELD_NAME = "urls";

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        RouterUtil.init();
    }

    /**
     * Dubbo启动时，获取并缓存当前服务的信息
     *
     * @param obj 增强的类
     * @param method 增强的方法
     * @param arguments 增强方法的所有参数
     * @param result the method's original return value. May be null if the method triggers an exception.
     * @return 返回值
     * @throws Exception 增强时可能出现的异常
     */
    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        // 获取监听类实例
        ServiceConfig<?> serviceConfig;
        if (obj instanceof ServiceConfig) {
            serviceConfig = (ServiceConfig<?>) obj;
            // 通过反射获取到cachedInvokerUrls变量，其中cachedInvokerUrls为应用的缓存
            List<URL> cachedUrls = RouterUtil.getField(ServiceConfig.class, List.class, serviceConfig, URLS_FIELD_NAME);
            setAppName(cachedUrls);
        }
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        dealException(throwable);
    }

    /**
     * 出现异常时，只记录日志
     *
     * @param throwable 异常信息
     */
    private void dealException(Throwable throwable) {
        LOGGER.log(Level.SEVERE, "ServiceConfig error!", throwable);
    }

    private void setAppName(List<URL> cachedUrls) {
        if (cachedUrls == null) {
            return;
        }
        // 将应用的缓存更新至本地缓存
        for (URL url : cachedUrls) {
            if (url == null) {
                continue;
            }
            DubboCache.setAppName(RouterUtil.getServiceName(url));
        }
    }
}
