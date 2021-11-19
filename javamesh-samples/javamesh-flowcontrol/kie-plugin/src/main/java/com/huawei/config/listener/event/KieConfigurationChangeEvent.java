/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.config.listener.event;

import java.util.EventObject;

/**
 * 配置改变事件
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class KieConfigurationChangeEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public KieConfigurationChangeEvent(Object source) {
        super(source);
    }
}
