/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.label.observers;

import java.util.Map;
import java.util.Properties;

/**
 * 标签库更新通知接口,当标签库监听到新的消息过来时，调用所有的观察者
 *
 * @author zhouss
 * @since 2021-07-01
 */
public interface LabelUpdateObserver {

    /**
     * 通知方法
     *
     * @param properties 标签属性
     */
    void notify(Map<String, Properties> properties);


    /**
     * 获取标签名
     * 由底层实现指定需要的标签名，仅当指定了对应的标签名并更新了数据才会通知观察者
     *
     * @return 标签名
     */
    String getLabelName();
}
