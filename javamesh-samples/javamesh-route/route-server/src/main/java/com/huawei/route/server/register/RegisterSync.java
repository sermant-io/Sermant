/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.register;

import com.huawei.route.server.entity.AbstractInstance;
import com.huawei.route.server.entity.AbstractService;
import com.huawei.route.server.entity.ServiceRegistrarMessage;

import java.util.Collection;
import java.util.Map;

/**
 * 注册中心信息同步
 *
 * @author zhouss
 * @since 2021-10-08
 */
public interface RegisterSync<S extends AbstractService<T>, T extends AbstractInstance> {
    /**
     * 同步注册中心数据，实时查询更新
     *
     * @return 返回服务及其实例数据
     */
    Map<String, S> sync();

    /**
     * 根据上报的数据更新相关数据
     *
     * @param serviceRegistrarMessages 上报数据
     */
    void update(Collection<ServiceRegistrarMessage> serviceRegistrarMessages);
}
