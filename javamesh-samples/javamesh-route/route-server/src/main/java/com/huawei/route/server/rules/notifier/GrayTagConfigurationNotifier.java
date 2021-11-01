/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.notifier;

import com.huawei.route.server.conditions.ZookeeperConfigCenterCondition;
import com.huawei.route.server.constants.RouteConstants;
import com.huawei.route.server.rules.GrayRuleManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * 灰度规则更新通知
 * 通知更新实例的灰度规则数据
 *
 * @author zhouss
 * @since 2021-10-21
 */
@Component("grayTagConfigurationNotifier")
@Conditional(ZookeeperConfigCenterCondition.class)
public class GrayTagConfigurationNotifier implements Notifier{
    @Autowired
    private ZookeeperPathNotifierManager zookeeperPathNotifierManager;

    @Autowired
    private PathDataUpdater pathDataUpdater;

    @SuppressWarnings("rawtypes")
    @Autowired
    private GrayRuleManager grayRuleManager;

    @PostConstruct
    public void registerListener() {
        zookeeperPathNotifierManager.registerTrigger(RouteConstants.TAG_NOTIFIER_PATH, this);
        updatePathData(null, null);
    }

    /**
     * 通过更新路径数据来通知所有的监听者更新本地的实例标签数据
     * 当其中一个参数为空时，则更细所有数据
     *
     * @param serviceName 标签服务名
     * @param instanceName 实例名
     */
    public void updatePathData(String serviceName, String instanceName) {
        pathDataUpdater.updatePathData(RouteConstants.TAG_NOTIFIER_PATH, generateData(serviceName, instanceName));
    }

    private byte[] generateData(String serviceName, String instanceName) {
        final long timeMillis = System.currentTimeMillis();
        if (StringUtils.isEmpty(serviceName) || StringUtils.isEmpty(instanceName)) {
            return String.valueOf(timeMillis).getBytes(StandardCharsets.UTF_8);
        }
        return String.format("%s%s%s%s%d", serviceName, RouteConstants.COMMON_SEPARATOR, instanceName,
                RouteConstants.COMMON_SEPARATOR, timeMillis).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void notify(String data) {
        if (StringUtils.isNotEmpty(data)) {
            final String[] parts = StringUtils.split(data, RouteConstants.COMMON_SEPARATOR);
            if (parts.length == RouteConstants.TAG_NOTIFIER_CONTENT_LEN) {
                // 取出服务等数据
                grayRuleManager.updateTagConfiguration(parts[0], parts[1]);
            } else {
                grayRuleManager.updateTagConfiguration(null, null);
            }
        }
    }
}
