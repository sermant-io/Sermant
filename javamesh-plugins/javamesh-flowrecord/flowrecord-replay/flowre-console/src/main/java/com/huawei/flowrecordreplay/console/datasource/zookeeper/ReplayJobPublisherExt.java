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

import com.huawei.flowrecordreplay.console.datasource.entity.ReplayJobEntity;
import com.huawei.flowrecordreplay.console.job.JobPublisher;

/**
 * zookeeper回放任务接口定义
 *
 * @author lilai
 * @version 0.0.1
 * @param <T>
 * @since 2021-02-26
 */
public interface ReplayJobPublisherExt<T> extends JobPublisher<T> {
    /**
     * 在ZK中查询回放任务
     *
     * @param jobId 任务ID
     * @return 任务详情
     */
    ReplayJobEntity getReplayJob(String jobId) throws Exception;

    /**
     * 在ZK中删除回放任务
     *
     * @param jobId 任务ID
     */
    void deleteReplayJob(String jobId) throws Exception;

    /**
     * 在ZK中删除回放待执行子任务
     *
     * @param jobId 任务ID
     */
    void deleteSubReplayJob(String jobId) throws Exception;

    /**
     * 往ZK中下发回放任务
     *
     * @param job 任务详情
     */
    @Override
    void publish(T job) throws Exception;
}
