/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.apm.core.dynamicconfig.zookeeper;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.apm.core.dynamicconfig.Config;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.dynamicconfig.DynamicConfiguration;
import com.huawei.apm.core.dynamicconfig.DynamicConfigurationFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;


/**
 *
 */
public class ZookeeperDynamicConfigurationFactory implements DynamicConfigurationFactory {

    private static final Logger logger = LogFactory.getLogger();

    @Override
    public DynamicConfiguration getDynamicConfiguration(URI uri) {

        ZookeeperDynamicConfiguration zdc = null;

        try {
            String zkAddress = uri.getHost();
            if ( uri.getPort() > 0 )
            {
                zkAddress += ":" + uri.getPort();
            }
            ZooKeeper zkClient;
            zkClient = new ZooKeeper(zkAddress, Config.TIMEOUT_VALUE, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    //logger.log(Level.WARNING, "Received event: " + event);
                    //Maybe should do nothing here.
                }
            });
            zdc = new ZookeeperDynamicConfiguration(zkClient, Config.DEFAULT_GROUP);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return zdc;
    }
}
