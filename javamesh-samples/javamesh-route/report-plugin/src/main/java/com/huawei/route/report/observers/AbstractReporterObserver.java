/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.observers;

import com.huawei.route.common.label.observers.LabelUpdateObserver;
import com.huawei.route.common.report.cache.ServiceRegisterCache;

/**
 * 上报模块标签配置更新器
 *
 * @author zhouss
 * @since 2021-11-02
 */
public abstract class AbstractReporterObserver implements LabelUpdateObserver {
    /**
     * 通知消息跟新
     */
    protected void comingMessage() {
        ServiceRegisterCache.getInstance().notifyLdcMessageComing();
    }
}
