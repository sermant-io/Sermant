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

package com.huawei.flowrecordreplay.console.controller;

import com.huawei.flowrecordreplay.console.datasource.elasticsearch.ElasticsearchJobStorage;
import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSource;
import com.huawei.flowrecordreplay.console.datasource.entity.RecordJobEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.ReplayJobEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.recordresult.RecordResultCountEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.recordresult.RecordResultEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.IgnoreFieldEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.ReplayResultCompareEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.ReplayResultCountEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.replayresult.ReplayResultDetailEntity;
import com.huawei.flowrecordreplay.console.datasource.entity.stresstest.FlowReplayMetric;
import com.huawei.flowrecordreplay.console.datasource.zookeeper.RecordJobZookeeperPublisher;
import com.huawei.flowrecordreplay.console.datasource.zookeeper.ReplayJobZookeeperPublisher;
import com.huawei.flowrecordreplay.console.datasource.zookeeper.ZookeeperUtil;
import com.huawei.flowrecordreplay.console.domain.CreateRecordJobRequest;
import com.huawei.flowrecordreplay.console.domain.CreateReplayJobRequest;
import com.huawei.flowrecordreplay.console.domain.RecordJobs;
import com.huawei.flowrecordreplay.console.domain.ReplayJobs;
import com.huawei.flowrecordreplay.console.domain.Result;
import com.huawei.flowrecordreplay.console.service.RecordResultService;
import com.huawei.flowrecordreplay.console.service.ReplayResultService;
import com.huawei.flowrecordreplay.console.service.StressTestResultService;
import com.huawei.flowrecordreplay.console.util.Constant;
import com.huawei.flowrecordreplay.console.util.IPUtil;

import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 录制和回放任务接口
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
@RestController
@RequestMapping("/jobs")
public class JobController {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);

    private static final String REPLAY_RESULT_PREFIX = "replay_result_";

    @Autowired
    private RecordJobZookeeperPublisher recordJobPublisher;

    @Autowired
    private ReplayJobZookeeperPublisher replayJobPublisher;

    @Autowired
    private ElasticsearchJobStorage elasticsearchJobStorage;

    @Autowired
    private ReplayResultService replayResultService;

    @Autowired
    private RecordResultService recordResultService;

    @Autowired
    private StressTestResultService stressTestResultService;

    @Autowired
    private EsDataSource esDataSource;

    @Autowired
    private CuratorFramework zkClient;

    /**
     * 下发录制任务
     *
     * @param request 录制任务请求
     * @return 返回结果信息
     */
    @PostMapping("/record-job")
    public Result<RecordJobEntity> addRecordJob(@RequestBody CreateRecordJobRequest request) {
        String message = checkRecordJobRequest(request);
        if (StringUtils.isNotBlank(message)) {
            return Result.ofFail(Constant.ERROR_CODE, message);
        }

        try {
            if (elasticsearchJobStorage.checkDocExistenceByKeyword(Constant.RECORD_JOB_INDEX,
                Constant.NAME_KEYWORD, request.getName())) {
                return Result.ofFail(Constant.ERROR_CODE, "Job name already exists.");
            }
        } catch (IOException e) {
            LOGGER.error("Fail to check name : {}", e.getMessage());
            return Result.ofFail(Constant.ERROR_CODE, "Fail to check name.");
        }

        try {
            if (elasticsearchJobStorage.checkPeriodValidation(request.getApplication(), request.getStartTime(),
                request.getEndTime())) {
                return Result.ofFail(Constant.ERROR_CODE, "Record time conflicts.");
            }
        } catch (IOException e) {
            LOGGER.error("Fail to check record time : {}", e.getMessage());
            return Result.ofFail(Constant.ERROR_CODE, "Fail to check record time.");
        }

        RecordJobEntity recordJob = new RecordJobEntity();
        try {
            recordJob.setName(request.getName());
            recordJob.setApplication(request.getApplication());
            recordJob.setMachineList(request.getMachineList());
            recordJob.setMethodList(request.getMethodList());
            recordJob.setStartTime(request.getStartTime());
            recordJob.setEndTime(request.getEndTime());
            recordJob.setTrigger(true);
            recordJob.setExtra(request.getExtra());
            recordJob.setStatus(Constant.PENDING_STATUS);
            recordJob.setTimeStamp(new Date().getTime());
            recordJob.setJobId(UUID.randomUUID().toString());

            elasticsearchJobStorage.insert(Constant.RECORD_JOB_INDEX, recordJob);
            recordJobPublisher.publish(recordJob);
        } catch (Exception e) {
            LOGGER.error("Fail to create record job: {}", e.getMessage());
            return Result.ofFail(Constant.ERROR_CODE, "Fail to create record job.");
        }

        LOGGER.info("Create record Job success, job ID : {}", recordJob.getJobId());
        return Result.ofSuccess(recordJob);
    }

    /**
     * 条件查询录制任务列表
     *
     * @param name        任务名称
     * @param application 应用名
     * @param from        查询起始时间
     * @param to          查询结束时间
     * @return 返回结果信息
     */
    @GetMapping("/record-jobs")
    public Result<RecordJobs> queryRecordJobList(@RequestParam(required = false) String name,
                                                 @RequestParam(required = false) String application,
                                                 @RequestParam(required = false) String from,
                                                 @RequestParam(required = false) String to) {
        if ((StringUtils.isBlank(from) && StringUtils.isNotBlank(to))
            || (StringUtils.isBlank(from) && StringUtils.isNotBlank(to))) {
            return Result.ofFail(Constant.ERROR_CODE, "Invalid Time input.");
        }
        RecordJobs jobs;
        try {
            jobs = elasticsearchJobStorage.getRecordJobList(name, application, from, to);
        } catch (Exception e) {
            LOGGER.error("Fail to query record job list : {}", e.getMessage());
            return Result.ofFail(Constant.ERROR_CODE, "Fail to query record job list.");
        }
        LOGGER.info("Query record job list success.");
        return Result.ofSuccess(jobs);
    }

    /**
     * 查询录制任务
     *
     * @param jobId 任务ID
     * @return 返回结果信息
     */
    @GetMapping("/record-job/{jobId}")
    public Result<RecordJobEntity> queryRecordJobById(@PathVariable String jobId) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }
        RecordJobEntity recordJob;
        try {
            recordJob = elasticsearchJobStorage.getRecordJob(jobId);
        } catch (IOException e) {
            LOGGER.error("Fail to query record job : {}, job ID : {}", e.getMessage(), jobId);
            return Result.ofFail(Constant.ERROR_CODE, "Fail to query record job.");
        }
        LOGGER.info("Query record job success, job ID : {}", jobId);
        return Result.ofSuccess(recordJob);
    }

    /**
     * 终止录制任务
     *
     * @param jobId 任务ID
     * @return 返回结果信息
     */
    @PutMapping("/record-job/stop/{jobId}")
    public Result<RecordJobEntity> stopRecordJob(@PathVariable String jobId) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }

        try {
            RecordJobEntity recordJob = elasticsearchJobStorage.getRecordJob(jobId);
            if (recordJob == null) {
                LOGGER.info("Can't find the record job, job ID : {}", jobId);
                return Result.ofFail(Constant.ERROR_CODE, "Can't find the record job.");
            }
            if (!recordJob.getStatus().equals(Constant.RUNNING_STATUS)) {
                LOGGER.info("Replay job is not running, can't be stopped, job ID : {}", jobId);
                return Result.ofFail(Constant.ERROR_CODE, "Replay job is not running, can't be stopped.");
            }
            recordJob.setTrigger(false);
            recordJobPublisher.updateCurrentRecordJob(recordJob);
        } catch (Exception e) {
            LOGGER.error("Fail to stop record job : {}, job ID : {}", e.getMessage(), jobId);
            return Result.ofFail(Constant.ERROR_CODE, "Fail to stop record job.");
        }
        LOGGER.info("Stop record job success, job ID : {}", jobId);
        return Result.ofSuccessMsg("Stop record job success.");
    }

    /**
     * 删除录制任务
     *
     * @param jobId 任务ID
     * @return 返回结果信息
     */
    @DeleteMapping("/record-job/{jobId}")
    public Result<RecordJobEntity> deleteRecordJob(@PathVariable String jobId) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }
        try {
            RecordJobEntity recordJob = elasticsearchJobStorage.getRecordJob(jobId);
            if (recordJob == null) {
                LOGGER.info("Can't find the record job, job ID : {}", jobId);
                return Result.ofFail(Constant.ERROR_CODE, "Can't find the record job.");
            }
            if (recordJob.getStatus().equals(Constant.RUNNING_STATUS)) {
                LOGGER.info("Replay job is running, can't be deleted, job ID : {}", jobId);
                return Result.ofFail(Constant.ERROR_CODE, "Replay job is running, can't be deleted.");
            }
            elasticsearchJobStorage.deleteRecordJob(jobId);
        } catch (IOException e) {
            LOGGER.error("Fail to delete record job : {}, job ID : {}", e.getMessage(), jobId);
            return Result.ofFail(Constant.ERROR_CODE, "Fail to delete record job.");
        }
        LOGGER.info("Delete record job success, job ID : {}", jobId);
        return Result.ofSuccessMsg("Delete record job success.");
    }

    /**
     * 下发回放任务
     *
     * @param request 回放任务请求
     * @return 返回结果信息
     */
    @PostMapping("/replay-job")
    public Result<ReplayJobEntity> addReplayJob(@RequestBody CreateReplayJobRequest request) {
        String message = checkReplayJobRequest(request);
        if (StringUtils.isNotBlank(message)) {
            return Result.ofFail(Constant.ERROR_CODE, message);
        }
        try {
            if (elasticsearchJobStorage.checkDocExistenceByKeyword(Constant.REPLAY_JOB_INDEX,
                Constant.NAME_KEYWORD, request.getName())) {
                return Result.ofFail(Constant.ERROR_CODE, "Job name already exists.");
            }
        } catch (IOException e) {
            LOGGER.error("Fail to check name : {}", e.getMessage());
            return Result.ofFail(Constant.ERROR_CODE, "Fail to check name.");
        }

        ReplayJobEntity replayJob = new ReplayJobEntity();

        try {
            RecordJobEntity recordJob = elasticsearchJobStorage.getRecordJob(request.getRecordJobId());
            if (recordJob == null) {
                LOGGER.error("Can't find the record job ：{}", request.getRecordJobId());
                return Result.ofFail(Constant.ERROR_CODE, "Can't find the record job.");
            }
            replayJob.setRecordJobName(recordJob.getName());
            replayJob.setApplication(recordJob.getApplication());
        } catch (IOException e) {
            LOGGER.error("Fail to query the record job ：{}, job ID : {}", e.getMessage(), request.getRecordJobId());
            return Result.ofFail(Constant.ERROR_CODE, "Fail to query the record job.");
        }

        try {
            replayJob.setRecordIndexList(elasticsearchJobStorage.getIndexList(request.getRecordJobId()));
        } catch (Exception e) {
            LOGGER.error("Can't find the specific record job index ：{}", e.getMessage());
            return Result.ofFail(Constant.ERROR_CODE, "Can't find the specific record job index.");
        }
        try {
            replayJob.setRecordJobId(request.getRecordJobId());
            replayJob.setTimeStamp(new Date().getTime());
            replayJob.setName(request.getName());
            replayJob.setFrom(request.getFrom());
            replayJob.setTo(request.getTo());
            replayJob.setAddress(request.getAddress());
            replayJob.setStatus(Constant.PENDING_STATUS);
            replayJob.setJobId(UUID.randomUUID().toString());
            replayJob.setModifyRule(request.getModifyRule());
            replayJob.setMockMethods(request.getMockMethods());
            replayJob.setStressTestType(request.getStressTestType());
            replayJob.setBaselineThroughPut(request.getBaselineThroughPut());
            replayJob.setMaxThreadCount(request.getMaxThreadCount());
            replayJob.setMaxResponseTime(request.getMaxResponseTime());
            replayJob.setMinSuccessRate(request.getMinSuccessRate());
            elasticsearchJobStorage.insert(Constant.REPLAY_JOB_INDEX, replayJob);
            replayJobPublisher.publish(replayJob);

            // 将指定mock的接口加入配置中心 /mock_methods/replayJobId/methodName
            for (String methodName : request.getMockMethods()) {
                ZookeeperUtil.setData(Constant.MOCK_METHODS_PREFIX + Constant.SPLIT
                    + replayJob.getJobId() + Constant.SPLIT + methodName, "", zkClient);
            }
        } catch (Exception e) {
            LOGGER.error("Fail to create replay job : {}", e.getMessage());
            return Result.ofFail(Constant.ERROR_CODE, "Fail to create replay job.");
        }
        LOGGER.info("Create replay job success, job ID : {}", replayJob.getJobId());
        return Result.ofSuccess(replayJob);
    }

    /**
     * 查询回放任务
     *
     * @param jobId 任务ID
     * @return 返回结果信息
     */
    @GetMapping("/replay-job/{jobId}")
    public Result<ReplayJobEntity> queryReplayJob(@PathVariable String jobId) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }
        ReplayJobEntity replayJob;
        try {
            replayJob = elasticsearchJobStorage.getReplayJob(jobId);
        } catch (IOException e) {
            LOGGER.error("Fail to query replay job : {}, job ID : {}", e.getMessage(), jobId);
            return Result.ofFail(Constant.ERROR_CODE, "Fail to query replay job.");
        }
        LOGGER.info("Query replay job success, job ID : {}", jobId);
        return Result.ofSuccess(replayJob);
    }

    /**
     * 条件查询回放任务列表
     *
     * @param name        任务名称
     * @param application 应用名
     * @return 返回结果信息
     */
    @GetMapping("/replay-jobs")
    public Result<ReplayJobs> queryReplayJobByApp(@RequestParam(required = false) String name,
                                                  @RequestParam(required = false) String application) {
        ReplayJobs jobs;
        try {
            jobs = elasticsearchJobStorage.getReplayJobList(name, application);
        } catch (IOException e) {
            LOGGER.error("Fail to query replay job list : {}", e.getMessage());
            return Result.ofFail(Constant.ERROR_CODE, "Fail to query replay job list.");
        }
        LOGGER.info("Query replay job list success.");
        return Result.ofSuccess(jobs);
    }

    /**
     * 删除回放任务
     *
     * @param jobId 任务ID
     * @return 返回结果信息
     */
    @DeleteMapping("/replay-job/{jobId}")
    public Result<RecordJobEntity> deleteReplayJob(@PathVariable String jobId) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }

        try {
            ReplayJobEntity replayJob = elasticsearchJobStorage.getReplayJob(jobId);
            if (replayJob == null) {
                LOGGER.info("Can't find the replay job, job ID : {}", jobId);
                return Result.ofFail(Constant.ERROR_CODE, "Can't find the replay job.");
            }

            if (replayJob.getStatus().equals(Constant.RUNNING_STATUS)) {
                LOGGER.info("Replay job is running, can't be deleted, job ID : {}", jobId);
                return Result.ofFail(Constant.ERROR_CODE, "Replay job is running, can't be deleted.");
            }

            replayJobPublisher.deleteSubReplayJob(jobId);
            elasticsearchJobStorage.deleteReplayJob(jobId);
        } catch (Exception e) {
            LOGGER.error("Fail to delete replay job : {}, job ID : {}", e.getMessage(), jobId);
            return Result.ofFail(Constant.ERROR_CODE, "Fail to delete replay job.");
        }
        LOGGER.info("Delete replay job success, job ID : {}", jobId);
        return Result.ofSuccessMsg("Delete replay job success.");
    }

    /**
     * 终止回放任务
     *
     * @param jobId 任务ID
     * @return 返回结果信息
     */
    @PutMapping("/replay-job/stop/{jobId}")
    public Result<RecordJobEntity> stopReplayJob(@PathVariable String jobId) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }

        try {
            ReplayJobEntity replayJob = elasticsearchJobStorage.getReplayJob(jobId);
            if (replayJob == null) {
                LOGGER.info("Can't find the replay job, job ID : {}", jobId);
                return Result.ofFail(Constant.ERROR_CODE, "Can't find the replay job.");
            }

            if (!replayJob.getStatus().equals(Constant.RUNNING_STATUS)) {
                LOGGER.info("Replay job is not running, can't be stopped, job ID : {}", jobId);
                return Result.ofFail(Constant.ERROR_CODE, "Replay job is not running, can't be stopped.");
            }

            replayJobPublisher.deleteSubReplayJob(jobId);
            elasticsearchJobStorage.stopReplayJob(jobId);
        } catch (Exception e) {
            LOGGER.error("Fail to stop replay job : {}, job ID : {}", e.getMessage(), jobId);
            return Result.ofFail(Constant.ERROR_CODE, "Fail to stop replay job.");
        }
        LOGGER.info("Stop replay job success, job ID : {}", jobId);
        return Result.ofSuccessMsg("Stop replay job success.");
    }

    /**
     * 检查必要参数
     *
     * @param request 录制任务请求
     * @return 返回提示信息
     */
    private String checkRecordJobRequest(CreateRecordJobRequest request) {
        if (request == null) {
            return "Invalid request body.";
        }
        if (StringUtils.isBlank(request.getName())) {
            return "Name can't be null or empty.";
        }
        if (StringUtils.isBlank(request.getApplication())) {
            return "Application can't be null or empty.";
        }
        if (CollectionUtils.isEmpty(request.getMachineList())) {
            return "MachineList can't be null or empty.";
        }
        if (request.getMethodList() == null) {
            return "MethodList can't be null.";
        }
        if (request.getStartTime() == null) {
            return "StartTime can't be null or empty.";
        }
        if (request.getEndTime() == null) {
            return "EndTime can't be null or empty.";
        }
        if (request.getStartTime().after(request.getEndTime())) {
            return "StartTime can't be later than endTime.";
        }
        for (String ip : request.getMachineList()) {
            if (!IPUtil.checkIPv4(ip)) {
                return "IP is invalid.";
            }
        }
        return "";
    }

    /**
     * 检查必要参数
     *
     * @param request 回放任务请求
     * @return 返回提示信息
     */
    private String checkReplayJobRequest(CreateReplayJobRequest request) {
        if (request == null) {
            return "Invalid request body.";
        }
        if (StringUtils.isBlank(request.getName())) {
            return "Name can't be null or empty.";
        }
        if (request.getRecordJobId() == null) {
            return "RecordJobId can't be null or empty.";
        }
        if (request.getFrom() == null) {
            return "Start Time Point can't be null or empty.";
        }
        if (request.getTo() == null) {
            return "End Time Point can't be null or empty.";
        }
        if (request.getFrom().after(request.getTo())) {
            return "Start time point can't be later than end time point.";
        }
        if (request.getAddress() == null) {
            return "Replay address can't be null or empty.";
        }
        if (request.getModifyRule() == null) {
            return "ModifyRule can't be null";
        }
        if (request.getMockMethods() == null) {
            return "MockMethods can't be null";
        }
        return "";
    }

    // 2021-04-06 结果比对接口

    /**
     * 回放结果统计
     *
     * @param jobId 回放任务id
     * @return 回放结果的统计
     */
    @GetMapping("/replay-jobs/result-overview/{jobId}")
    public Result<ReplayResultCountEntity> replayResultCount(@PathVariable String jobId) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }
        if (!esDataSource.checkIndexExistence(REPLAY_RESULT_PREFIX + jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job replay result is not exist.");
        }
        ReplayResultCountEntity replayResultCountEntity = replayResultService.getReplayOverview(jobId);
        return Result.ofSuccess(replayResultCountEntity);
    }

    /**
     * 获取详细的结果比对
     *
     * @param jobId   回放任务id
     * @param method  接口名称
     * @param correct 筛选正确的和错误的
     * @return 返回一个回放结果的json比对
     */
    @GetMapping("/replay-jobs/result/{jobId}")
    public Result<ReplayResultCompareEntity> replayResult(@PathVariable String jobId,
                                                          @RequestParam(required = false) String method,
                                                          @RequestParam(required = false) String correct) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }
        if (!esDataSource.checkIndexExistence(REPLAY_RESULT_PREFIX + jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job replay result is not exist.");
        }
        List<JSONObject> replayResultCompareEntityList;
        replayResultCompareEntityList = replayResultService.getReplayResultCompare(jobId, method, correct);
        ReplayResultCompareEntity replayResultCompareEntity = new ReplayResultCompareEntity();
        replayResultCompareEntity.setReplayResultCompareEntityList(replayResultCompareEntityList);
        replayResultCompareEntity.setTotal(replayResultCompareEntityList.size());
        return Result.ofSuccess(replayResultCompareEntity);
    }

    /**
     * 查看一条请求的详细回放结果
     *
     * @param jobId   回放任务的id
     * @param method  回放的接口
     * @param traceId 回放请求的id
     * @return 返回一条请求的详细结果比对和对应接口的忽略情况
     */
    @GetMapping("/replay-jobs/result-detail/{jobId}")
    public Result<ReplayResultDetailEntity> replayResultDetail(@PathVariable String jobId,
                                                               @RequestParam String method,
                                                               @RequestParam String traceId) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }

        if (StringUtils.isBlank(method)) {
            return Result.ofFail(Constant.ERROR_CODE, "Method can't be null or empty.");
        }

        if (StringUtils.isBlank(traceId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Trace ID can't be null or empty.");
        }

        if (!esDataSource.checkIndexExistence(REPLAY_RESULT_PREFIX + jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job replay result is not exist.");
        }

        ReplayResultDetailEntity replayResultDetailEntity = replayResultService
            .getReplayResultDetail(jobId, traceId, method);
        return Result.ofSuccess(replayResultDetailEntity);
    }

    /**
     * 重新进行结果比对
     *
     * @param jobId  回放任务id
     * @param method 回放的接口
     * @return 返回重新比对后的结果
     */
    @GetMapping("/replay-jobs/re-compare/{jobId}")
    public Result<String> reCompare(@PathVariable String jobId, @RequestParam(required = false) String method) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }
        if (!esDataSource.checkIndexExistence(REPLAY_RESULT_PREFIX + jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job replay result is not exist.");
        }
        if (!StringUtils.isBlank(method)) {
            replayResultService.reCompare(jobId, method);
        } else {
            replayResultService.reCompare(jobId);
        }
        return Result.ofSuccess("ReCompare successful.");
    }

    /**
     * 上传忽略字段
     *
     * @param ignoreFieldEntity 忽略字段数据结构
     * @return 返回提示信息
     */
    @PostMapping("/replay-jobs/ignore")
    public Result<String> ignoreFiled(@RequestBody IgnoreFieldEntity ignoreFieldEntity) {
        boolean isUpdate = replayResultService.ignoreFiled(ignoreFieldEntity);
        if (isUpdate) {
            return Result.ofSuccess("Update ignore fields successful.");
        } else {
            return Result.ofFail(Constant.ERROR_CODE, "Update ignore fields failed.");
        }
    }

    /**
     * 获取回放响应时间的结果统计
     *
     * @param jobId 回放任务Id
     * @return 响应时间的统计
     */
    @GetMapping("/replay-jobs/response-time-statistics/{jobId}")
    public Result<Map<String, Long>> getResponseTimeStatistics(@PathVariable String jobId) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }
        if (!esDataSource.checkIndexExistence(Constant.REPLAY_RESULT_PREFIX + jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job replay result is not exist.");
        }
        return Result.ofSuccess(stressTestResultService.getResponseTimeStatistics(jobId));
    }

    /**
     * 获取回放响应时间的结果统计
     *
     * @param jobId 回放任务Id
     * @return 回访任务的回放节点指标
     */
    @GetMapping("/replay-jobs/replay-metrics/{jobId}")
    public Result<List<FlowReplayMetric>> getFlowReplayMetrics(@PathVariable String jobId) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }
        if (!esDataSource.checkIndexExistence(Constant.REPLAY_METRIC)) {
            return Result.ofFail(Constant.ERROR_CODE, "Replay metric is not exist.");
        }
        return Result.ofSuccess(stressTestResultService.getFlowReplayMetricList(jobId));
    }

    /**
     * 录制结果统计
     *
     * @param jobId 回放任务id
     * @return 回放结果的统计
     */
    @GetMapping("/record-jobs/result-overview/{jobId}")
    public Result<RecordResultCountEntity> recordResultCount(@PathVariable String jobId) throws IOException {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }
        if (!esDataSource.checkIndexExistence(jobId + Constant.STAR)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job record result is not exist.");
        }
        RecordResultCountEntity recordResultCountEntity = recordResultService.getRecordOverview(jobId);
        return Result.ofSuccess(recordResultCountEntity);
    }

    /**
     * 获取详细的录制数据
     *
     * @param jobId     回放任务id
     * @param method    接口名称
     * @param traceId   链路id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 返回一个回放结果的json比对
     */
    @GetMapping("/record-jobs/result/{jobId}")
    public Result<RecordResultEntity> recordResult(@PathVariable String jobId,
                                                   @RequestParam(required = false) String method,
                                                   @RequestParam(required = false) String traceId,
                                                   @RequestParam(required = false) String startTime,
                                                   @RequestParam(required = false) String endTime) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }
        if ((startTime != null && endTime == null) || (startTime == null && endTime != null)) {
            return Result.ofFail(Constant.ERROR_CODE, "startTime and endTime should exist together.");
        }

        if (!esDataSource.checkIndexExistence(jobId + Constant.STAR)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job record result is not exist");
        }
        RecordResultEntity recordResultEntity = new RecordResultEntity();
        recordResultEntity = recordResultService.getRecordResult(jobId, method, traceId, startTime, endTime);
        return Result.ofSuccess(recordResultEntity);
    }

    /**
     * 获取任务中的全部子调用接口
     *
     * @param jobId 录制任务id
     * @return 返回子调用接口的list
     */
    @GetMapping("/record-jobs/sub-methods/{jobId}")
    public Result<List<String>> getSubMethods(@PathVariable String jobId) {
        if (StringUtils.isBlank(jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job ID can't be null or empty.");
        }
        if (!esDataSource.checkIndexExistence(Constant.SUB_CALL_PREFIX + jobId)) {
            return Result.ofFail(Constant.ERROR_CODE, "Job record result is not exist");
        }
        return Result.ofSuccess(recordResultService.getSubCallMethods(jobId));
    }
}