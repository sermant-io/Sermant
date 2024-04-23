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

package io.sermant.spring.feign.api.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * 针对header方法增加请求头判断是否可以匹配成功
 *
 * @author zhouss
 * @since 2022-07-29
 */
public class HeaderMatchConfiguration implements RequestInterceptor {
    private static final String KEY = "key";

    @Override
    public void apply(RequestTemplate template) {
        final String url = template.url();
        if (url.contains("headerExact")) {
            template.header(KEY, "flowControlExact");
        } else if (url.contains("headerPrefix")) {
            template.header(KEY, "flowControlPrefix");
        } else if (url.contains("headerSuffix")) {
            template.header(KEY, "flowControlSuffix");
        } else if (url.contains("headerContains")) {
            template.header(KEY, "flowControlContains");
        } else if (url.contains("headerCompareMatch")) {
            template.header(KEY, "102");
        } else if (url.contains("headerCompareNotMatch")) {
            template.header(KEY, "100");
        }
    }
}
