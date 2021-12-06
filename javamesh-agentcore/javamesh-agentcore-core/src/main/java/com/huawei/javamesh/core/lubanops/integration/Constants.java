/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.lubanops.integration;

import java.nio.charset.Charset;

/**
 * 包含系统常量值的类
 */
public class Constants {

    /**
     * 系统支持的最大的消息长度，当前是1M
     */
    public final static int MAX_MESSAGE_LENGTH = 1024 * 1024;

    /**
     * 默认的监控项的阻塞队列的长度
     */
    public final static int DEFAULT_MONITOR_QUEUE_SIZE = 100;

    /**
     *
     **/
    public final static int DEFAULT_MONITOR_THREAD_COUNT = 1;

    /**
     * 默认的监控项的阻塞队列的长度
     */
    public final static int DEFAULT_EVENT_QUEUE_SIZE = 1000;

    /**
     * 默认的监控项的阻塞队列的长度
     */
    public final static int DEFAULT_EVENT_THREAD_COUNT = 1;

    /**
     * 同步发送数据的超时时间
     */
    public final static int SYNC_SEND_MESSAGE_TIMEOUT = 2000;

    /**
     * 字符集
     */
    public final static String DEFAULT_ENCODING = "UTF-8";

    public final static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

}
