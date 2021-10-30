/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.conditions;

import com.huawei.route.server.config.ConfigCenterEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 配置中心使用zookeeper进行全局标签规则配置同步
 *
 * @author zhouss
 * @since 2021-10-12
 */
public class ZookeeperConfigCenterCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String configCenter = conditionContext.getEnvironment().getProperty("route.server.gray.config-center");
        return StringUtils.isEmpty(configCenter) || StringUtils.equals(configCenter,
                ConfigCenterEnum.ZOOKEEPER.getConfigCenter());
    }
}
