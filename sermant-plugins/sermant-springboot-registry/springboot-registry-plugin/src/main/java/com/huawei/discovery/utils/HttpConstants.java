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

package com.huawei.discovery.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 参数标签类
 *
 * @author chengyouling
 * @since 2022-09-14
 */
public class HttpConstants {

    /**
     * get请求
     */
    public static final String HTTP_GET = "GET";

    /**
     * 从url解析的服务名
     */
    public static final String HTTP_URI_SERVICE = "serviceName";

    /**
     * 域名
     */
    public static final String HTTP_URI_HOST = "host";

    /**
     * 路径
     */
    public static final String HTTP_URI_PATH = "path";

    /**
     * 双斜杠
     */
    public static final String HTTP_URL_DOUBLE_SLASH = "://";

    /**
     * 冒号
     */
    public static final String HTTP_URL_COLON = ":";

    /**
     * 问号
     */
    public static final String HTTP_URL_UNKNOWN = "?";

    /**
     * 单斜杠
     */
    public static final char HTTP_URL_SINGLE_SLASH = '/';

    /**
     * 协议
     */
    public static final String EMPTY_STR = "";

    /**
     * 协议
     */
    public static final String HTTP_URL_SCHEME = "scheme";

    /**
     * 时间格式
     */
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private HttpConstants() {

    }

    /**
     *
     * 获取当前时间
     *
     * @return 时间
     */
    public static String currentTime() {
        return SIMPLE_DATE_FORMAT.format(new Date());
    }
}
