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

package com.huawei.flowrecordreplay.console.datasource.zookeeper;

import com.huawei.flowrecordreplay.console.datasource.entity.RecordJobEntity;
import com.huawei.flowrecordreplay.console.util.Constant;

import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * zookeeper录制任务接口实现
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
@Component
public class RecordJobZookeeperPublisher implements RecordJobPublisherExt<RecordJobEntity> {
    @Autowired
    private CuratorFramework zkClient;

    /**
     * 往ZK中下发录制任务
     */
    @Override
    public void publish(RecordJobEntity recordJob) throws Exception {
        String path = Constant.RECORD_PATH_PREFIX + Constant.SPLIT + recordJob.getApplication() + Constant.SPLIT
                + recordJob.getJobId();
        ZookeeperUtil.setData(path, recordJob, zkClient);
    }

    /**
     * 在ZK中查询录制任务
     */
    @Override
    public RecordJobEntity getRecordJob(String application, String jobId) throws Exception {
        String path = Constant.RECORD_PATH_PREFIX + Constant.SPLIT + application + Constant.SPLIT + jobId;
        return JSON.parseObject(ZookeeperUtil.getData(path, zkClient),
                RecordJobEntity.class);
    }

    /**
     * 在ZK中删除录制任务
     */
    @Override
    public void deleteRecordJob(String application, String jobId) throws Exception {
        String path = Constant.RECORD_PATH_PREFIX + Constant.SPLIT + application + Constant.SPLIT + jobId;
        zkClient.delete().forPath(path);
    }

    /**
     * 更新当前任务
     */
    @Override
    public void updateCurrentRecordJob(RecordJobEntity recordJob) throws Exception {
        String path = Constant.RECORD_PATH_PREFIX + Constant.SPLIT + recordJob.getApplication() + Constant.SPLIT
                + Constant.CURRENT_JOB_NODE;
        ZookeeperUtil.setData(path, recordJob, zkClient);
    }
}