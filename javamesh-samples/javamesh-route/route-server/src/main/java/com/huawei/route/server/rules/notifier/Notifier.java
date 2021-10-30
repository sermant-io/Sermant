/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules.notifier;

/**
 * 通知器
 * 暂时支持zookeeper
 * {@link ZookeeperPathNotifierManager}
 *
 * @author zhouss
 * @since 2021-10-21
 */
public interface Notifier {
    /**
     * 通知数据变更
     *
     * @param data 变更后的数据
     */
    void notify(String data);

}
