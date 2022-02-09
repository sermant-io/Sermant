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

package com.huawei.sermant.core.lubanops.bootstrap.commons;

import java.nio.charset.Charset;

/**
 * @author
 * @date 2020/9/22 16:04
 */
public class LubanApmConstants {

    public final static String LUBANOPS_APM_PRODUCT_NAME = "apm";

    public final static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public final static int DEFAULT_SERVICE_SHUTDOWN_TIMEOUT = 1 * 1000;

    public final static int AGENT_BOOTSTRAP_REGISTER_RETRY_INTERVAL = 100;

    public final static int AGENT_BOOTSTRAP_REGISTER_RETRY_TIMES = 10;

    // ~~ data type
    public final static String SPAN_EVENT_DATA_TYPE = "SpanEventData";

    public final static String MONITOR_DATA_TYPE = "MonitorData";

    // ~~ config key
    public final static String CONFIG_FILENAME = "bootstrap.properties";

    public final static String APP_NAME_COMMONS = "appName";

    public final static String INSTANCE_NAME_COMMONS = "instanceName";

    public final static String ENV_COMMONS = "env";

    public final static String ENV_TAG_COMMONS = "envTag";

    public final static String BIZ_PATH_COMMONS = "business";

    public final static String SUB_BUSINESS_COMMONS = "subBusiness";

    public final static String ENV_SECRET_COMMONS = "envSecret";

    public final static String AGENT_PATH_COMMONS = "agentPath";

    public final static String BOOT_PATH_COMMONS = "bootPath";

    public final static String PLUGINS_PATH_COMMONS = "pluginsPath";

    public final static String APP_TYPE_COMMON = "appType";

    /**
     * 运行 backend 模块机器的ip
     */
    public static final String BACKEND_IP_COMMON = "127.0.0.1";

    /**
     * backend 模块 netty 服务 监听的端口
     */
    public static final String BACKEND_PORT_COMMON = "6888";
}
