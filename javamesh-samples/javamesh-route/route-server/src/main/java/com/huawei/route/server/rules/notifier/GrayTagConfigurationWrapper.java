/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.notifier;


import com.huawei.route.server.labels.vo.LabelBusinessVo;
import com.huawei.route.server.labels.vo.LabelVo;
import com.huawei.route.server.config.RouteServerProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 该类主要处理通知数据
 * 当用户对指定的标签名进行数据修改时，通知到{@link GrayTagConfigurationNotifier} 更新节点数据
 * 该块包裹{@link GrayTagConfigurationNotifier}, 主要应对配置中心非zookeeper的场景
 *
 * =====可考虑使用切面嵌入LabelService
 *
 * @author zhouss
 * @since 2021-10-21
 */
@Component
public class GrayTagConfigurationWrapper {
    @Autowired(required = false)
    private GrayTagConfigurationNotifier grayTagConfigurationNotifier;

    @Autowired
    private RouteServerProperties routeServerProperties;

    /**
     * 更新节点路径
     * 仅当前灰度规则路径进行更新、删除、添加等操作时调用该模块
     *
     * @param labelGroupName 标签组名
     * @param labelName 标签名
     */
    public void updateTagListenPath(String labelGroupName, String labelName) {
        if (grayTagConfigurationNotifier != null && isGrayLabel(labelGroupName, labelName)) {
            // 仅当为灰度规则才进行更新路径数据
            grayTagConfigurationNotifier.updatePathData(null, null);
        }
    }

    /**
     * 更新节点路径
     * 仅当前灰度规则路径进行更新、删除、添加等操作时调用该模块
     *
     * @param labelVo 操作对象
     */
    public void updateTagListenPath(LabelVo labelVo) {
        updateTagListenPath(labelVo.getLabelGroupName(), labelVo.getLabelName());
    }

    /**
     * 生效单实例通知更新
     *
     * @param labelBusinessVo 更新数据
     */
    public void updateTagListenPath(LabelBusinessVo labelBusinessVo) {
        if (labelBusinessVo != null && grayTagConfigurationNotifier != null
                && isGrayLabel(labelBusinessVo.getLabelGroupName(), labelBusinessVo.getLabelName())) {
            // 仅当为灰度规则才进行更新路径数据
            grayTagConfigurationNotifier.updatePathData(labelBusinessVo.getServiceName(), labelBusinessVo.getInstanceName());
        }
    }

    /**
     * 是否为灰度标签
     *
     * @param labelGroupName 标签组
     * @param labelName 标签名
     * @return 是否为灰度标签
     */
    private boolean isGrayLabel(String labelGroupName, String labelName) {
        return StringUtils.equals(labelGroupName, routeServerProperties.getGray().getRouteGroupName())
                && StringUtils.equals(labelName, routeServerProperties.getGray().getGrayLabelName());
    }

}
