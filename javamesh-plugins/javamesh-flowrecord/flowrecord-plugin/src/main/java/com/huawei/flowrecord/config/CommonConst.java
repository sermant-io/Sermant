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

package com.huawei.flowrecord.config;

/**
 * 普通常量类
 */
public class CommonConst {
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;

    /**
     * 重试间隔
     */
    public static final int SLEEP_TIME = 1000;

    /**
     * 冒号符号
     */
    public static final String COLON_SIGN = ":";

    /**
     * 斜杠符号
     */
    public static final String SLASH_SIGN = "/";

    /**
     * 点符号
     */
    public static final String POINT_SIGN = ".";

    /**
     * 逗号符号
     */
    public static final String COMMA_SIGN = ",";

    /**
     * 上下文传递的path
     */
    public static final String PATH = "path";

    /**
     * 404
     */
    public static final int SERVICE_NOT_EXIST = 404;

    /**
     *
     */
    public static final int FLOW_RECORD_DATA_TYPE = 3;
}
