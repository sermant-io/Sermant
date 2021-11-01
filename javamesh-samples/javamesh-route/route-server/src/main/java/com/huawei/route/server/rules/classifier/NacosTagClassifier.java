/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.classifier;

import com.huawei.route.server.conditions.NacosSyncCondition;
import com.huawei.route.server.constants.RouteConstants;
import com.huawei.route.server.register.nacos.NacosInstance;
import com.huawei.route.server.register.nacos.NacosService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * nacos数据归类
 *
 * @author zhouss
 * @since 2021-10-14
 */
@Component
@ConditionalOnClass(NacosSyncCondition.class)
public class NacosTagClassifier implements TagClassifier<NacosService, NacosInstance> {

    @Override
    public Map<String, NacosService> classifier(Map<String, NacosService> registerInfo) {
        // 关联上报数据， 该块暂时无需求
        // 对实例数据分组存放
        for (NacosService nacosService : registerInfo.values()) {
            final Map<String, List<NacosInstance>> ldcGroupInstances = nacosService.getInstanceHolders().values().stream()
                    .map(nacosInstanceInstanceHolder -> nacosInstanceInstanceHolder.getInstances().values())
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.groupingBy(nacosInstance -> {
                String ldc = nacosInstance.getLdc();
                if (StringUtils.isEmpty(ldc)) {
                    return RouteConstants.DEFAULT_LDC;
                }
                return ldc;
            }));
            nacosService.setLdcInstances(ldcGroupInstances);
        }
        return registerInfo;
    }
}
