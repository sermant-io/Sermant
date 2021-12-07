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

package com.huawei.flowrecordreplay.console.datasource.elasticsearch;

import com.huawei.flowrecordreplay.console.datasource.entity.RecordJobEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.ReplayJobEntity;
import com.huawei.flowrecordreplay.console.domain.RecordJobs;
import com.huawei.flowrecordreplay.console.domain.ReplayJobs;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * elasticsearch接口定义
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
public interface ElasticsearchJobStorage {
    /**
     * 条件查询录制任务
     *
     * @param name        任务名称
     * @param application 应用名
     * @param from        查询起始时间
     * @param to          查询终止时间
     * @return 返回录制任务列表
     */
    RecordJobs getRecordJobList(String name, String application, String from, String to)
            throws IOException, ParseException;

    /**
     * 根据录制任务ID查询索引列表
     *
     * @param jobId 任务ID
     * @return 返回索引列表
     */
    List<String> getIndexList(String jobId) throws IOException;

    /**
     * 根据ID查询录制任务
     *
     * @param jobId 任务ID
     * @return 返回录制任务详情
     */
    RecordJobEntity getRecordJob(String jobId) throws IOException;

    /**
     * 查询即将进行的录制任务
     *
     * @return 返回录制任务列表
     */
    RecordJobs getComingRecordJob() throws IOException;

    /**
     * 查询索引是否存在
     *
     * @param index es索引
     * @return 是否存在
     */
    boolean checkIndexExistence(String index) throws IOException;

    /**
     * 往ES中写入数据
     *
     * @param index es索引
     * @param data  插入的数据
     */
    void insert(String index, Object data) throws IOException;

    /**
     * 删除录制任务
     *
     * @param jobId 任务ID
     */
    void deleteRecordJob(String jobId) throws IOException;

    /**
     * 删除回放任务
     *
     * @param jobId 任务ID
     */
    void deleteReplayJob(String jobId) throws IOException;

    /**
     * 删除回放子任务
     *
     * @param jobId 任务ID
     */
    void deleteSubReplayJob(String jobId) throws IOException;

    /**
     * 根据回放任务ID查询回放任务
     *
     * @param jobId 任务ID
     * @return 返回回放任务详情
     */
    ReplayJobEntity getReplayJob(String jobId) throws IOException;

    /**
     * 条件查询回放任务列表
     *
     * @param name        任务名称
     * @param application 应用名
     * @return 返回回放任务列表
     */
    ReplayJobs getReplayJobList(String name, String application) throws IOException;

    /**
     * 终止回放任务
     *
     * @param jobId 任务ID
     */
    void stopReplayJob(String jobId) throws IOException;

    /**
     * 查询回放worker的job信息
     *
     * @param worker worker名称
     * @return 返回任务信息
     */
    Map<String, String> getJobByWorker(String worker) throws IOException;

    /**
     * 更新录制任务es字段
     *
     * @param jobId 任务ID
     * @param key 更新字段
     * @param value 更新字段值
     */
    void updateRecordJob(String jobId, String key, String value) throws IOException;

    /**
     * 更新回放任务es字段
     *
     * @param jobId 任务ID
     * @param key 更新字段
     * @param value 更新字段值
     */
    void updateReplayJob(String jobId, String key, String value) throws IOException;

    /**
     * 检查es某个字段值的唯一性
     *
     * @param index es索引
     * @param key 检查的字段
     * @param value 检查的字段值
     * @return 是否唯一
     */
    boolean checkDocExistenceByKeyword(String index, String key, Object value) throws IOException;

    /**
     * 检查同一应用下录制任务时间是否冲突
     *
     * @param application 应用
     * @param start 起始时间
     * @param end 终止时间
     * @return 是否冲突
     */
    boolean checkPeriodValidation(String application, Date start, Date end) throws IOException;
}
