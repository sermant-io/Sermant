/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.register;

import com.huawei.route.server.conditions.ZookeeperConfigCenterCondition;
import com.huawei.route.server.constants.RouteConstants;
import com.huawei.route.server.entity.AbstractInstance;
import com.huawei.route.server.entity.AbstractService;
import com.huawei.route.server.entity.ServiceRegistrarMessage;
import com.huawei.route.server.rules.notifier.ZookeeperPathNotifierManager;
import com.huawei.route.server.share.RouteSharer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;

/**
 * 通知部分功能处理
 *
 * @author zhouss
 * @since 2021-10-28
 */
@Component
@Conditional(ZookeeperConfigCenterCondition.class)
public abstract class AbstractRegisterSync<S extends AbstractService<T>, T extends AbstractInstance>
        implements RegisterSync<S, T>{
    @Autowired
    private ZookeeperPathNotifierManager zookeeperPathNotifierManager;

    @Resource(name = "redisRouteSharer")
    private RouteSharer<ServiceRegistrarMessage> redisRouteSharer;

    @PostConstruct
    public void register() {
        zookeeperPathNotifierManager.registerTrigger(RouteConstants.SHARE_NOTIFIER_PATH, data -> {
            final Collection<ServiceRegistrarMessage> shareDataList =
                    redisRouteSharer.getShareDataList(ServiceRegistrarMessage.class);
            update(shareDataList);
        });
    }
}
