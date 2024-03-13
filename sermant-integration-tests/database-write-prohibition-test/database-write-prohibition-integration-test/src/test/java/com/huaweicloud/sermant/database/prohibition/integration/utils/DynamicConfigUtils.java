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

package com.huaweicloud.sermant.database.prohibition.integration.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;

/**
 * Distribute tools for dynamic configuration
 *
 * @author daizhenyu
 * @since 2024-03-12
 **/
public class DynamicConfigUtils {
    public static final String ZOOKEEPER_NODE_PATH = "/app=default&environment=/sermant.database.write.globalConfig";

    private DynamicConfigUtils() {
    }

    public static void updateConfig(String config) throws Exception {
        updateConfig(ZOOKEEPER_NODE_PATH, config);
    }

    public static void updateConfig(String path, String config) throws Exception {
        CuratorFramework curator = CuratorFrameworkFactory.newClient("127.0.0.1:2181",
                new ExponentialBackoffRetry(1000, 3));
        curator.start();
        Stat stat = curator.checkExists().forPath(path);
        if (stat == null) {
            curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path,
                    config.getBytes(StandardCharsets.UTF_8));
        } else {
            curator.setData().forPath(path, config.getBytes(StandardCharsets.UTF_8));
        }
    }
}
