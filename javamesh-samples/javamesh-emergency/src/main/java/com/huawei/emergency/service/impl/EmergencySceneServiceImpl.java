/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyExecRecordExample;
import com.huawei.emergency.mapper.EmergencyExecRecordMapper;
import com.huawei.emergency.service.EmergencyPlanService;
import com.huawei.emergency.service.EmergencySceneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Resource;

/**
 * @author y30010171
 * @since 2021-11-04
 **/
@Service
@Transactional
public class EmergencySceneServiceImpl implements EmergencySceneService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmergencySceneServiceImpl.class);

    @Resource(name = "scriptExecThreadPool")
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private EmergencyExecRecordMapper execRecordMapper;

    @Autowired
    private ExecRecordHandlerFactory handlerFactory;

    @Autowired
    private EmergencyPlanService planService;

    @Override
    public void onComplete(EmergencyExecRecord record) {
        LOGGER.debug("Scene exec_id={},plan_id={},scene_id={} is finished.", record.getExecId(), record.getPlanId(), record.getSceneId());

        // 如果预案已经完成
        EmergencyExecRecordExample isPlanFinishedCondition = new EmergencyExecRecordExample();
        isPlanFinishedCondition.createCriteria()
            .andExecIdEqualTo(record.getExecId())
            .andPlanIdEqualTo(record.getPlanId())
            .andStatusIn(Arrays.asList("0", "1", "3", "4"));
        if (execRecordMapper.countByExample(isPlanFinishedCondition) == 0) {
            planService.onComplete(record);
            return;
        }

        // 判断当前场景是否完成，完成则执行依赖此场景的场景,否则执行子任务
        if (isSceneFinished(record.getExecId(), record.getSceneId())) {
            // 执行依赖此场景的场景
            EmergencyExecRecordExample needExecTaskCondition = new EmergencyExecRecordExample();
            needExecTaskCondition.createCriteria()
                .andExecIdEqualTo(record.getExecId())
                .andPreSceneIdEqualTo(record.getSceneId())
                .andTaskIdIsNull()
                .andStatusEqualTo("0")
                .andIsValidEqualTo("1");
            List<EmergencyExecRecord> needExecTasks = execRecordMapper.selectByExample(needExecTaskCondition);
            needExecTasks.forEach(execRecord -> {
                LOGGER.debug("Submit record_id={}. exec_id={}, task_id={}.", execRecord.getRecordId(), execRecord.getExecId(), execRecord.getTaskId());
                threadPoolExecutor.execute(handlerFactory.handle(execRecord));
            });
        } else {
            EmergencyExecRecordExample subTask = new EmergencyExecRecordExample();
            subTask.createCriteria()
                .andExecIdEqualTo(record.getExecId())
                .andIsValidEqualTo("1")
                .andStatusEqualTo("0")
                .andPreTaskIdIsNull()
                .andParentTaskIdEqualTo(record.getSceneId());
            List<EmergencyExecRecord> emergencyExecRecords = execRecordMapper.selectByExample(subTask);
            emergencyExecRecords.forEach(execRecord -> {
                LOGGER.debug("Submit record_id={}. exec_id={}, task_id={}.", execRecord.getRecordId(), execRecord.getExecId(), execRecord.getTaskId());
                threadPoolExecutor.execute(handlerFactory.handle(execRecord));
            });
        }
        LOGGER.debug("threadPoolExecutor = {} ", threadPoolExecutor);
    }

    @Override
    public boolean isSceneFinished(int execId, int sceneId) {
        EmergencyExecRecordExample isFinishedCondition = new EmergencyExecRecordExample();
        isFinishedCondition.createCriteria()
            .andExecIdEqualTo(execId)
            .andSceneIdEqualTo(sceneId)
            .andIsValidEqualTo("1")
            .andStatusIn(Arrays.asList("0", "1", "3", "4"));
        return execRecordMapper.countByExample(isFinishedCondition) == 0;
    }
}
