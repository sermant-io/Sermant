/*
 *  Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.database.constant;

/**
 * database type constant
 *
 * @author daizhenyu
 * @since 2024-02-06
 **/
public enum DatabaseType {
    /**
     * mongodb
     */
    MONGODB("MongoDB"),
    /**
     * mysql
     */
    MYSQL("MySQL"),
    /**
     * postgresql
     */
    POSTGRESQL("PostgreSQL"),
    /**
     * opengauss
     */
    OPENGAUSS("openGauss");

    private String type;

    /**
     * construction method
     *
     * @param type type of database
     */
    DatabaseType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
