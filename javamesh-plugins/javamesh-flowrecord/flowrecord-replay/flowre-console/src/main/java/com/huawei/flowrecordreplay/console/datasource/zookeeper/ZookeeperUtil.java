/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowrecordreplay.console.datasource.zookeeper;

import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;

/**
 * zookeeper工具类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
public class ZookeeperUtil {
    // ZK中写数据
    public static void setData(String path, Object obj, CuratorFramework zkClient) throws Exception {
        // 判断路径是否存在
        Stat stat = zkClient.checkExists().forPath(path);

        // 不存在，则新建永久节点
        if (stat == null) {
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
        }

        byte[] data = obj == null ? "[]" .getBytes(StandardCharsets.UTF_8)
                : JSONObject.toJSONString(obj).getBytes(StandardCharsets.UTF_8);

        // 发送数据
        zkClient.setData().forPath(path, data);
    }

    // 从zk中获取数据
    public static String getData(String path, CuratorFramework zkClient) throws Exception {
        Stat stat = zkClient.checkExists().forPath(path);

        if (stat == null) {
            return "";
        }

        byte[] bytes = zkClient.getData().forPath(path);
        if ((bytes == null) || (bytes.length == 0)) {
            return "";
        }

        return new String(bytes);
    }
}
