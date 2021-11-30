/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.resolver.listener;

import com.huawei.flowcontrol.adapte.cse.rule.Configurable;

import java.util.Map;

/**
 * CSE规则配置更新通知
 *
 * @author zhouss
 * @since 2021-11-24
 */
public interface ConfigUpdateListener<T extends Configurable> {
    /**
     * 规则配置更新通知
     *
     * @param rules 所有规则
     */
    void notify(Map<String, T> rules);
}
