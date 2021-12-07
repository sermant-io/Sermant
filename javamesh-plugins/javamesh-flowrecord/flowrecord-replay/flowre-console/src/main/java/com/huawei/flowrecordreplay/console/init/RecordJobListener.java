/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowrecordreplay.console.init;

import com.huawei.flowrecordreplay.console.datasource.elasticsearch.ElasticsearchJobStorage;
import com.huawei.flowrecordreplay.console.datasource.entity.RecordJobEntity;
import com.huawei.flowrecordreplay.console.datasource.zookeeper.ZookeeperUtil;
import com.huawei.flowrecordreplay.console.util.Constant;

import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * zookeeper监听器录制任务节点并更新任务状态
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-03-23
 */
@Component
public class RecordJobListener implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordJobListener.class);

    @Autowired
    CuratorFramework zkClient;

    @Autowired
    ElasticsearchJobStorage elasticsearchJobStorage;

    @Override
    public void run(ApplicationArguments args) {
        TreeCache cache = new TreeCache(zkClient, Constant.RECORD_PATH_PREFIX);
        try {
            cache.getListenable().addListener(new TreeCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event)
                        throws Exception {
                    handleUpdateEvent(event);
                    handleCreateEvent(event);
                }
            });
            cache.start();
        } catch (Exception e) {
            LOGGER.error("Failed to start zookeeper record job listener.", e);
        }
    }

    private void handleUpdateEvent(TreeCacheEvent event) throws Exception {
        if (event.getType().equals(TreeCacheEvent.Type.NODE_UPDATED)
                && event.getData().getPath().endsWith("_status")) {
            int index = event.getData().getPath().lastIndexOf(Constant.SPLIT);
            String status = ZookeeperUtil.getData(event.getData().getPath(), zkClient);
            String path = event.getData().getPath().substring(0, index);
            RecordJobEntity recordJob = JSON.parseObject(ZookeeperUtil.getData(path, zkClient),
                    RecordJobEntity.class);
            switch (status) {
                case Constant.RUNNING_STATUS:
                    elasticsearchJobStorage.updateRecordJob(recordJob.getJobId(),
                            Constant.STATUS_KEYWORD, Constant.RUNNING_STATUS);
                    LOGGER.info("Update record job status : RUNNING.");
                    break;

                case Constant.STOPPED_STATUS: {
                    List<String> children = zkClient.getChildren().forPath(path);
                    int count = 0;
                    for (String child : children) {
                        if (!ZookeeperUtil.getData(path + Constant.SPLIT + child, zkClient)
                                .equals(Constant.STOPPED_STATUS)) {
                            break;
                        }
                        if (count == children.size() - 1) {
                            elasticsearchJobStorage.updateRecordJob(recordJob.getJobId(),
                                    Constant.STATUS_KEYWORD, Constant.STOPPED_STATUS);
                            LOGGER.info("Update record job status : STOPPED");
                        }
                        count++;
                    }
                    break;
                }

                case Constant.DONE_STATUS: {
                    List<String> children = zkClient.getChildren().forPath(path);
                    int count = 0;
                    for (String child : children) {
                        String childStatus = ZookeeperUtil.getData(path + Constant.SPLIT + child, zkClient);
                        if (!Constant.DONE_STATUS.equals(childStatus) &&
                                !Constant.UNHANDLED_STATUS.equals(childStatus)) {
                            break;
                        }
                        if (count == children.size() - 1) {
                            elasticsearchJobStorage.updateRecordJob(recordJob.getJobId(),
                                    Constant.STATUS_KEYWORD, Constant.DONE_STATUS);
                            LOGGER.info("Update record job status : DONE");
                        }
                        count++;
                    }
                    break;
                }

                case Constant.UNHANDLED_STATUS: {
                    List<String> children = zkClient.getChildren().forPath(path);
                    int count = 0;
                    for (String child : children) {
                        String childStatus = ZookeeperUtil.getData(path + Constant.SPLIT + child, zkClient);
                        if (!Constant.UNHANDLED_STATUS.equals(childStatus)) {
                            break;
                        }
                        if (count == children.size() - 1) {
                            elasticsearchJobStorage.updateRecordJob(recordJob.getJobId(),
                                    Constant.STATUS_KEYWORD, Constant.UNHANDLED_STATUS);
                            LOGGER.info("Update record job status : UNHANDLED");
                        }
                        count++;
                    }
                    break;
                }

                default:
                    LOGGER.info("No operation for node update event.");
            }
        }
    }

    private void handleCreateEvent(TreeCacheEvent event) throws Exception {
        if (event.getType().equals(TreeCacheEvent.Type.NODE_ADDED)
                && event.getData().getPath().endsWith("_status")) {
            int index = event.getData().getPath().lastIndexOf(Constant.SPLIT);
            String status = ZookeeperUtil.getData(event.getData().getPath(), zkClient);
            if (Constant.RUNNING_STATUS.equals(status)) {
                String path = event.getData().getPath().substring(0, index);
                RecordJobEntity recordJob = JSON.parseObject(ZookeeperUtil.getData(path, zkClient),
                        RecordJobEntity.class);
                elasticsearchJobStorage.updateRecordJob(recordJob.getJobId(),
                        Constant.STATUS_KEYWORD, Constant.RUNNING_STATUS);
                LOGGER.info("Update record job status : RUNNING.");
            }
            LOGGER.info("No operation for node create event.");
        }
    }
}
