/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.stresstest.config.bean;

import com.mongodb.ServerAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库信息
 *
 * @author yiwei
 * @since 2021-10-21
 */
public class MongoSourceInfo extends UserInfo {
    private String host;

    private String database;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * 解析host,生成可用的ServerAddress
     *
     * @return mongodb serverAddress 地址
     */
    public List<ServerAddress> getAddresses() {
        List<ServerAddress> addresses = new ArrayList<>();
        String[] hostArray = getHost().split(",");
        for (String hostStr : hostArray) {
            String[] info = hostStr.replace(" ", "").split(":");
            addresses.add(new ServerAddress(info[0], Integer.parseInt(info[1])));
        }
        return addresses;
    }
}
