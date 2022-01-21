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

package com.huawei.flowcontrol.common.adapte.cse.match;

import java.util.Map;

/**
 * 匹配器
 *
 * @author zhouss
 * @since 2021-11-16
 */
public interface Matcher {
    /**
     * 匹配
     *
     * @param url 请求地址
     * @param headers 请求头
     * @param method 方法类型
     * @return 是否匹配
     */
    boolean match(String url, Map<String, String> headers, String method);
}
