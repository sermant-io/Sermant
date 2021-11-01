/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.classifier;

import com.huawei.route.server.entity.AbstractInstance;
import com.huawei.route.server.entity.AbstractService;
import com.huawei.route.server.entity.ServiceRegistrarMessage;
import com.huawei.route.server.rules.InstanceTagConfiguration;

import java.util.Map;

/**
 * 数据归类处理
 * 同时需要处理以下数据
 * 1、agent上报的数据  {@link ServiceRegistrarMessage}
 * 2、全局标签数据 {@link com.huawei.route.server.rules.GrayRuleManager}
 * 3、局部单实例标签数据 {@link InstanceTagConfiguration}
 *
 * 基于不同注册中心实现接口
 *
 * @author zhouss
 * @since 2021-10-14
 */
public interface TagClassifier<S extends AbstractService<T>, T extends AbstractInstance> {

    /**
     * LDC归类数据
     *
     * @param registerInfo 注册表同步数据
     * @return 归类后数据
     */
    Map<String, S> classifier(Map<String, S> registerInfo);
}
