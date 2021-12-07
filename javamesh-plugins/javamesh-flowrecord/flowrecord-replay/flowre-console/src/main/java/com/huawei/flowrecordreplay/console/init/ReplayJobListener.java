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
import com.huawei.flowrecordreplay.console.datasource.entity.ReplayJobEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.SubReplayJobEntity;
import com.huawei.flowrecordreplay.console.util.Constant;

import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * zookeeper监听器监听/replay_workers的子节点
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-03-23
 */
@Component
public class ReplayJobListener implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReplayJobListener.class);

    @Autowired
    CuratorFramework zkClient;

    @Autowired
    ElasticsearchJobStorage elasticsearchJobStorage;

    @Override
    public void run(ApplicationArguments args) {
        TreeCache cache = new TreeCache(zkClient, Constant.REPLAY_WORKERS_PATH_PREFIX);
        try {
            cache.getListenable().addListener(new TreeCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event)
                        throws Exception {
                    // 监听到子节点删除事件则把正在处理的任务重新放入zk待执行任务队列中
                    if (event.getType().equals(TreeCacheEvent.Type.NODE_REMOVED)) {
                        String[] stringArray = event.getData().getPath().split(Constant.SPLIT);
                        String worker = stringArray[stringArray.length - 1];
                        LOGGER.info("Replay worker lose connect, worker name : {}", worker);

                        Map<String, String> map = elasticsearchJobStorage.getJobByWorker(worker);
                        if (!map.isEmpty()) {
                            ReplayJobEntity replayJob = elasticsearchJobStorage
                                    .getReplayJob(map.get(Constant.JOB_ID_KEYWORD));
                            SubReplayJobEntity subReplayJobEntity = new SubReplayJobEntity();
                            subReplayJobEntity.setJobId(replayJob.getJobId());
                            subReplayJobEntity.setRecordJobId(replayJob.getRecordJobId());
                            subReplayJobEntity.setApplication(replayJob.getApplication());
                            subReplayJobEntity.setAddress(replayJob.getAddress());
                            subReplayJobEntity.setRecordIndex(map.get(Constant.RECORD_INDEX_KEYWORD));
                            subReplayJobEntity.setFrom(replayJob.getFrom());
                            subReplayJobEntity.setTo(replayJob.getTo());
                            subReplayJobEntity.setTimeStamp(replayJob.getTimeStamp());
                            String subJobQueuePathPrefix = Constant.SUB_REPLAY_JOB_PATH_PREFIX
                                    + Constant.REPLAY_SUB_JOB_NODE_PREFIX;
                            zkClient.create().creatingParentContainersIfNeeded()
                                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                                    .forPath(subJobQueuePathPrefix, JSONObject.toJSONString(subReplayJobEntity)
                                            .getBytes(StandardCharsets.UTF_8));
                            elasticsearchJobStorage.deleteSubReplayJob(map.get(Constant.JOB_ID_KEYWORD));
                            LOGGER.info("The replay sub job of the disconnecting replay worker is reset to queue, worker name : {}", worker);

                        }
                    }
                }
            });
            cache.start();
        } catch (Exception e) {
            LOGGER.error("Failed to start zookeeper replay job listener.", e);
        }
    }
}
