/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.config.bean;

import com.mongodb.ServerAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库信息
 *
 * @author yiwei
 * @since 2021/10/21
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
