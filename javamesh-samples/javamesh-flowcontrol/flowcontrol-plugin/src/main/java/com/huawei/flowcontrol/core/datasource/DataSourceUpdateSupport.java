/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.core.datasource;

import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;

/**
 * 数据源更新支持
 *
 * @author zhouss
 * @since 2021-11-26
 */
public interface DataSourceUpdateSupport {
    /**
     * 更新规则
     *
     * @param event 事件
     */
    void update(ConfigChangedEvent event);
}
