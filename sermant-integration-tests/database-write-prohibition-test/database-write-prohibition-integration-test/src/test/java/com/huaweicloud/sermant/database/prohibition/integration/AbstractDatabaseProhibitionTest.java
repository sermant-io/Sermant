/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.database.prohibition.integration;

import com.huaweicloud.sermant.database.prohibition.integration.entity.Result;
import com.huaweicloud.sermant.database.prohibition.integration.utils.HttpRequestUtils;

import com.alibaba.fastjson.JSONObject;

import org.junit.jupiter.api.Assertions;

/**
 * Database write prohibition test
 *
 * @author zhp
 * @since 2024-03-13
 */
public abstract class AbstractDatabaseProhibitionTest {
    /**
     * enable database write prohibition configuration
     */
    public static final String ENABLE_DATABASE_WRITE_PROHIBITION_CONFIG = "enablePostgreSqlWriteProhibition: true\n"
            + "enableOpenGaussWriteProhibition: true\n"
            + "postgreSqlDatabases:\n"
            + "  - test\n"
            + "openGaussDatabases:\n"
            + "  - postgres";

    /**
     * disable database write prohibition configuration
     */
    public static final String DISABLE_DATABASE_WRITE_PROHIBITION_CONFIG = "";

    /**
     * test interface results
     *
     * @param httpRequestUrl Request Address
     * @param statusCode 预期结果编码
     * @return 结果
     */
    public static Result testHttpRequest(String httpRequestUrl, String statusCode) {
        String response = HttpRequestUtils.doGet(httpRequestUrl);
        Result result = JSONObject.parseObject(response, Result.class);
        Assertions.assertEquals(result.getCode(), statusCode);
        return result;
    }
}
