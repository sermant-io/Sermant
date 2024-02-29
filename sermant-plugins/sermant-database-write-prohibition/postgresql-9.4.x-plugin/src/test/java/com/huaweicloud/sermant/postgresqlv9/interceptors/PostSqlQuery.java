/*
 *  Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.postgresqlv9.interceptors;

import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;

/**
 * Database SQL Query Implementation
 *
 * @author zhp
 * @since 2024-02-17
 **/
public class PostSqlQuery implements Query {
    private final String sql;

    public PostSqlQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public ParameterList createParameterList() {
        return null;
    }

    @Override
    public String toString(ParameterList parameters) {
        return sql;
    }

    @Override
    public void close() {
        // Test data write prohibition, no specific shutdown function required
    }

    @Override
    public boolean isStatementDescribed() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        return sql;
    }
}
