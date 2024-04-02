/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.utils.tag;

import java.util.List;
import java.util.Map;

/**
 * TrafficData
 *
 * @author lilai
 * @since 2023-07-26
 */
public class TrafficData extends TrafficTag {
    private final String path;

    private final String httpMethod;

    /**
     * constructor
     *
     * @param header headers/attachments
     * @param path path
     * @param httpMethod httpMethod
     */
    public TrafficData(Map<String, List<String>> header, String path, String httpMethod) {
        super(header);
        this.path = path;
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    @Override
    public String toString() {
        return "{"
                + "path='" + path + '\''
                + ", httpMethod='" + httpMethod + '\''
                + ", tag='" + getTag() + '\''
                + '}';
    }
}
