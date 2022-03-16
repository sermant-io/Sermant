/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.backend.common.exception;

/**
 * zookeeper动态配置异常类
 *
 * @author 薛泽超
 * @since 2022-03-16
 */
public class ZookeeperDynamicConfigurationException extends Exception {
    private static final long serialVersionUID = -940403875065143157L;

    /**
     * zookeeper 动态配置异常
     *
     * @param msg 异常信息
     */
    public ZookeeperDynamicConfigurationException(String msg) {
        super(msg);
    }
}
