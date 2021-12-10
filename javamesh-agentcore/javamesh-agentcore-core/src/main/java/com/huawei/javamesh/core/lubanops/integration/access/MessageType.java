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

package com.huawei.javamesh.core.lubanops.integration.access;

/**
 * @author
 * @since 2020/4/30
 **/
public class MessageType {

    /**
     * 监控数据上报
     */
    public static final short MONITOR_DATA_REQUEST = 1;

    public static final short MONITOR_DATA_RESPONSE = 2;

    /**
     * event 数据上报
     */
    public static final short TRACE_EVENT_REQUEST = 3;

    public static final short TRACE_EVENT_RESPONSE = 4;

    /**
     * javaagent链接access server的时候返回的消息，一般是链接错误或者正确的消息
     */
    public static final short ACCESS_SESSION_OPEN_RESPONSE = 5;

    /**
     * 查看javaagent的采集器的状态的消息
     */
    public static final short ACCESS_COLLECTOR_STATUS_REQUEST = 15;

    public static final short ACCESS_COLLECTOR_STATUS_RESPONSE = 16;

    /**
     * 查看是否是response的消息，需要通过合格来判断是否需要实现同步通知的服务
     *
     * @return
     */
    public static boolean isResponseMessage(short type) {
        if (type == MONITOR_DATA_RESPONSE || type == TRACE_EVENT_RESPONSE || type == ACCESS_COLLECTOR_STATUS_RESPONSE) {
            return true;
        }
        return false;
    }
}
