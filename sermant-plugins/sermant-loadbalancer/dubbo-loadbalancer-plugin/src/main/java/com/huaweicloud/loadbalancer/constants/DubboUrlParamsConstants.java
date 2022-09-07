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

package com.huaweicloud.loadbalancer.constants;

/**
 * dubbo url参数常量, 见
 * <p/>
 * org.apache.dubbo.common.URL#getParameter
 * <p/>
 * org.apache.dubbo.common.URL#getParameter
 *
 * @author zhouss
 * @since 2022-09-13
 */
public class DubboUrlParamsConstants {
    /**
     * 接口
     */
    public static final String DUBBO_INTERFACE = "interface";

    /**
     * 服务名
     */
    public static final String DUBBO_APPLICATION = "application";

    /**
     * 远程服务名
     */
    public static final String DUBBO_REMOTE_APPLICATION = "remote.application";

    /**
     * 负载均衡
     */
    public static final String DUBBO_LOAD_BALANCER_KEY = "loadbalance";

    private DubboUrlParamsConstants() {
    }
}
