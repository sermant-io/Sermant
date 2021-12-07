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

import com.huawei.flowrecordreplay.console.datasource.entity.ReplayJobEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.SubReplayJobEntity;
import com.huawei.flowrecordreplay.console.util.Constant;

import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * zookeeper回放任务接口实现
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
@Component
public class ReplayJobZookeeperPublisher implements ReplayJobPublisherExt<ReplayJobEntity> {
    @Autowired
    private CuratorFramework zkClient;

    /**
     * 往ZK中下发回放任务
     */
    @Override
    public void publish(ReplayJobEntity replayJob) throws Exception {
        String path = Constant.REPLAY_PATH_PREFIX + Constant.SPLIT + replayJob.getJobId();
        ZookeeperUtil.setData(path, replayJob, zkClient);

        if (zkClient.checkExists().forPath(Constant.SUB_REPLAY_JOB_PATH_PREFIX) == null) {
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT)
                .forPath(Constant.SUB_REPLAY_JOB_PATH_PREFIX);
        }

        for (String index : replayJob.getRecordIndexList()) {
            String subJobPath = path + Constant.SPLIT + index;
            ZookeeperUtil.setData(subJobPath, null, zkClient);

            SubReplayJobEntity subReplayJobEntity = new SubReplayJobEntity();
            subReplayJobEntity.setJobId(replayJob.getJobId());
            subReplayJobEntity.setRecordJobId(replayJob.getRecordJobId());
            subReplayJobEntity.setApplication(replayJob.getApplication());
            subReplayJobEntity.setAddress(replayJob.getAddress());
            subReplayJobEntity.setFrom(replayJob.getFrom());
            subReplayJobEntity.setTo(replayJob.getTo());
            subReplayJobEntity.setTimeStamp(replayJob.getTimeStamp());
            subReplayJobEntity.setRecordIndex(index);
            subReplayJobEntity.setModifyRule(replayJob.getModifyRule());
            subReplayJobEntity.setStressTestType(replayJob.getStressTestType());
            subReplayJobEntity.setBaselineThroughPut(replayJob.getBaselineThroughPut());
            subReplayJobEntity.setMaxThreadCount(replayJob.getMaxThreadCount());
            subReplayJobEntity.setMaxResponseTime(replayJob.getMaxResponseTime());
            subReplayJobEntity.setMinSuccessRate(replayJob.getMinSuccessRate());

            String subJobQueuePathPrefix = Constant.SUB_REPLAY_JOB_PATH_PREFIX + Constant.REPLAY_SUB_JOB_NODE_PREFIX;
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                .forPath(subJobQueuePathPrefix, JSONObject.toJSONString(subReplayJobEntity)
                    .getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 在ZK中查询回放任务
     */
    @Override
    public ReplayJobEntity getReplayJob(String jobId) throws Exception {
        String path = Constant.REPLAY_PATH_PREFIX + Constant.SPLIT + jobId;
        return JSON.parseObject(ZookeeperUtil.getData(path, zkClient), ReplayJobEntity.class);
    }

    /**
     * 在ZK中删除回放任务
     */
    @Override
    public void deleteReplayJob(String jobId) throws Exception {
        String path = Constant.REPLAY_PATH_PREFIX + Constant.SPLIT + jobId;
        if (zkClient.checkExists().forPath(path) != null) {
            zkClient.delete().deletingChildrenIfNeeded().forPath(path);
        }
    }

    /**
     * 在ZK中删除回放待执行子任务
     */
    @Override
    public void deleteSubReplayJob(String jobId) throws Exception {
        List<String> children = zkClient.getChildren().forPath(Constant.SUB_REPLAY_JOB_PATH_PREFIX);
        for (String child : children) {
            String path = Constant.SUB_REPLAY_JOB_PATH_PREFIX + Constant.SPLIT + child;
            String data = ZookeeperUtil.getData(path, zkClient);
            if (StringUtils.isNotBlank(data)) {
                SubReplayJobEntity subReplayJobEntity = JSON.parseObject(data, SubReplayJobEntity.class);
                if (subReplayJobEntity.getJobId().equals(jobId)) {
                    zkClient.delete().forPath(path);
                }
            }
        }
    }
}