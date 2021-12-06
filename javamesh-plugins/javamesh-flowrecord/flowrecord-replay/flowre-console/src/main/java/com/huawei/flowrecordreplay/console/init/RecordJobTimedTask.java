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
import com.huawei.flowrecordreplay.console.datasource.zookeeper.RecordJobZookeeperPublisher;
import com.huawei.flowrecordreplay.console.domain.RecordJobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 定时任务更新当前录制任务
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-03-29
 */
@Component
public class RecordJobTimedTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordJobTimedTask.class);

    @Autowired
    ElasticsearchJobStorage elasticsearchJobStorage;

    @Autowired
    RecordJobZookeeperPublisher publisher;

    @Scheduled(fixedDelay = 30000)
    public void refreshCurrentRecordJob() {
        LOGGER.info("Timed task: start to refresh current record job...");
        RecordJobs jobs = null;
        try {
            jobs = elasticsearchJobStorage.getComingRecordJob();
        } catch (IOException e) {
            LOGGER.error("Failed to get coming record job.", e);
        }
        if (jobs != null && jobs.getTotal() != 0) {
            for (RecordJobEntity job : jobs.getJobs()) {
                try {
                    publisher.updateCurrentRecordJob(job);
                } catch (Exception e) {
                    LOGGER.error("Failed to update current record job.", e);
                }
            }
            LOGGER.info("Timed task: refresh current record job success.");
        }
    }
}
