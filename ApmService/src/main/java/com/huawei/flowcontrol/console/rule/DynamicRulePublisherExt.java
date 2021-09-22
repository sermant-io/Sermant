/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.rule;

/**
 * publisher接口
 *
 * @param <T>
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
public interface DynamicRulePublisherExt<T> {
    /**
     * Publish rules to remote rule configuration center for given application name.
     *
     * @param app   app name
     * @param rules list of rules to push
     * @throws Exception if some error occurs
     */
    void publish(String app, T rules) throws Exception;

    /**
     * publish 规则
     *
     * @param app        应用名
     * @param entityType 规则类型
     * @param rules      规则集合
     * @throws Exception zookeeper forpath 异常
     */
    void publish(String app, String entityType, T rules) throws Exception;
}
