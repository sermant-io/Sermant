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

package com.huaweicloud.sermant.database.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * SqlParserUtils Unit Test
 *
 * @author daizhenyu
 * @since 2024-02-06
 **/
public class SqlParserUtilsTest {
    private String sql;

    @Test
    public void testIsWriteOperation() {
        // sql is a write operation
        sql = "INSERT INTO table (name) VALUES ('test')";
        Assert.assertTrue(SqlParserUtils.isWriteOperation(sql));

        sql = "CREATE TABLE table (name VARCHAR(255))";
        Assert.assertTrue(SqlParserUtils.isWriteOperation(sql));

        sql = "DROP INDEX idx_name on table";
        Assert.assertTrue(SqlParserUtils.isWriteOperation(sql));

        // sql is a read operation
        sql = "SELECT * FROM table";
        Assert.assertFalse(SqlParserUtils.isWriteOperation(sql));
    }
}
