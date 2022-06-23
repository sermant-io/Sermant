/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.nacos.common;

/**
 * 公共变量
 *
 * @author zhouss
 * @since 2022-06-20
 */
public class CommonConstants {
    /**
     * ip
     */
    public static final String IP_KEY = "provider.ip";

    /**
     * 服务名
     */
    public static final String SERVICE_NAME_KEY = "provider.serviceName";

    /**
     * 端口
     */
    public static final String PORT_KEY = "provider.port";

    /**
     * 是否开启预热
     */
    public static final String WARM_UP_STATE = "provider.warmUp";

    /**
     * 预热环境变量名称
     */
    public static final String WARM_UP_ENVIRONMENT = "grace.rule.enableWarmUp";

    /**
     * 聚合开关
     */
    public static final String WARM_UP_ENVIRONMENT_AGG = "grace.rule.enableGrace";

    /**
     * QPS
     */
    public static final String QPS_KEY = "provider.qps";

    private CommonConstants() {
    }
}
