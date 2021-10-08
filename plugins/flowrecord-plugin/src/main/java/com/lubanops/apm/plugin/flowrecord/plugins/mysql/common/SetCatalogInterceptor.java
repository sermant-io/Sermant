/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.common;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.lubanops.apm.bootstrap.TransformAccess;
import com.mysql.cj.util.StringUtils;
import org.apache.skywalking.apm.plugin.jdbc.trace.ConnectionInfo;

import java.lang.reflect.Method;

/**
 * 在connection info中添加数据库名
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-02
 */
public class SetCatalogInterceptor implements InstanceMethodInterceptor {

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        TransformAccess objInst = (TransformAccess) obj;
        Object dynamicField = objInst.getLopsAttribute();
        if (dynamicField instanceof ConnectionInfo
                && StringUtils.isNullOrEmpty(((ConnectionInfo) dynamicField).getDatabaseName())) {
            ((ConnectionInfo) dynamicField).setDatabaseName(String.valueOf(arguments[0]));
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {

    }
}
