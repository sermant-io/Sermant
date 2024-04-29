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

package io.sermant.database.config;

import java.util.HashSet;
import java.util.Set;

/**
 * Database prohibit writing plugin configuration classes
 *
 * @author daizhenyu
 * @since 2024-01-15
 **/
public class DatabaseWriteProhibitionConfig {
    /**
     * MongoDb whether to enable write prohibition
     */
    private boolean enableMongoDbWriteProhibition = false;

    /**
     * MongoDb requires writing forbidden databases
     */
    private Set<String> mongoDbDatabases = new HashSet<>();

    /**
     * Mysql whether to enable write prohibition
     */
    private boolean enableMySqlWriteProhibition = false;

    /**
     * Mysql requires writing forbidden databases
     */
    private Set<String> mySqlDatabases = new HashSet<>();

    /**
     * PostgreSQL whether to enable write prohibition
     */
    private boolean enablePostgreSqlWriteProhibition = false;

    /**
     * PostgreSQL requires writing forbidden databases
     */
    private Set<String> postgreSqlDatabases = new HashSet<>();

    /**
     * OpenGauss whether to enable write prohibition
     */
    private boolean enableOpenGaussWriteProhibition = false;

    /**
     * OpenGauss requires writing forbidden databases
     */
    private Set<String> openGaussDatabases = new HashSet<>();

    public boolean isEnableMongoDbWriteProhibition() {
        return enableMongoDbWriteProhibition;
    }

    public void setEnableMongoDbWriteProhibition(boolean enableMongoDbWriteProhibition) {
        this.enableMongoDbWriteProhibition = enableMongoDbWriteProhibition;
    }

    public Set<String> getMongoDbDatabases() {
        return mongoDbDatabases;
    }

    public void setMongoDbDatabases(Set<String> mongoDbDatabases) {
        this.mongoDbDatabases = mongoDbDatabases;
    }

    public boolean isEnableMySqlWriteProhibition() {
        return enableMySqlWriteProhibition;
    }

    public void setEnableMySqlWriteProhibition(boolean enableMySqlWriteProhibition) {
        this.enableMySqlWriteProhibition = enableMySqlWriteProhibition;
    }

    public Set<String> getMySqlDatabases() {
        return mySqlDatabases;
    }

    public void setMySqlDatabases(Set<String> mySqlDatabases) {
        this.mySqlDatabases = mySqlDatabases;
    }

    public boolean isEnablePostgreSqlWriteProhibition() {
        return enablePostgreSqlWriteProhibition;
    }

    public void setEnablePostgreSqlWriteProhibition(boolean enablePostgreSqlWriteProhibition) {
        this.enablePostgreSqlWriteProhibition = enablePostgreSqlWriteProhibition;
    }

    public Set<String> getPostgreSqlDatabases() {
        return postgreSqlDatabases;
    }

    public void setPostgreSqlDatabases(Set<String> postgreSqlDatabases) {
        this.postgreSqlDatabases = postgreSqlDatabases;
    }

    public boolean isEnableOpenGaussWriteProhibition() {
        return enableOpenGaussWriteProhibition;
    }

    public void setEnableOpenGaussWriteProhibition(boolean enableOpenGaussWriteProhibition) {
        this.enableOpenGaussWriteProhibition = enableOpenGaussWriteProhibition;
    }

    public Set<String> getOpenGaussDatabases() {
        return openGaussDatabases;
    }

    public void setOpenGaussDatabases(Set<String> openGaussDatabases) {
        this.openGaussDatabases = openGaussDatabases;
    }

    @Override
    public String toString() {
        return "enableMongoDbWriteProhibition=" + enableMongoDbWriteProhibition
                + ", mongoDbDatabases=" + mongoDbDatabases + "; "
                + "enableMysqlWriteProhibition=" + enableMySqlWriteProhibition
                + ", mysqlDatabases=" + mySqlDatabases + "; "
                + "enablePostgreSqlWriteProhibition=" + enablePostgreSqlWriteProhibition
                + ", postgreSqlDatabases=" + postgreSqlDatabases + ";"
                + " enableOpenGaussWriteProhibition=" + enableOpenGaussWriteProhibition
                + ", openGaussDatabases=" + openGaussDatabases;
    }
}
