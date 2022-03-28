/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
