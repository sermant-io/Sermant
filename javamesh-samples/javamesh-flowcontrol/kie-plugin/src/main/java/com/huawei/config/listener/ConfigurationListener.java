/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.config.listener;

import java.util.EventListener;
import java.util.EventObject;

/**
 * 监听器
 *
 * @author zhouss
 * @since 2021-11-17
 */
public interface ConfigurationListener extends EventListener {

    /**
     * 配置更新
     *
     * @param object 事件源
     */
    void onEvent(EventObject object);
}
