/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.huawei.common.api.CommonResult;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyExecRecordExample;
import com.huawei.emergency.entity.EmergencyExecRecordWithBLOBs;
import com.huawei.emergency.mapper.EmergencyExecRecordMapper;
import com.huawei.emergency.service.EmergencySceneService;
import com.huawei.emergency.service.EmergencyTaskDetailService;
import com.huawei.emergency.service.EmergencyTaskService;
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
@Transactional(rollbackFor = Exception.class)
public class EmergencyTaskDetailServiceImpl implements EmergencyTaskDetailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmergencyTaskDetailServiceImpl.class);

    @Resource(name = "scriptExecThreadPool")
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private EmergencyTaskService taskService;

    @Autowired
    private EmergencySceneService sceneService;

    @Autowired
    private EmergencyExecRecordMapper execRecordMapper;

    @Autowired
    private ExecRecordHandlerFactory handlerFactory;

    @Override
    public void onComplete(EmergencyExecRecord record) {
        LOGGER.debug("ExecRecord {} is finished. exec_id={}.", record.getRecordId(), record.getExecId());

        // 判断任务是否完成
        EmergencyExecRecordExample isTaskFinishedCondition = new EmergencyExecRecordExample();
        isTaskFinishedCondition.createCriteria()
            .andExecIdEqualTo(record.getExecId())
            .andTaskIdEqualTo(record.getTaskId())
            .andStatusIn(Arrays.asList("0", "1", "3", "4"));
        if (execRecordMapper.countByExample(isTaskFinishedCondition) == 0) {
            taskService.onComplete(record);
            return;
        }

        // 执行依赖当前子任务的子任务
        EmergencyExecRecordExample needExecCondition = new EmergencyExecRecordExample();
        needExecCondition.createCriteria()
            .andExecIdEqualTo(record.getExecId());
            //.andPreDetailIdEqualTo(record.getTaskDetailId());
        List<EmergencyExecRecord> needExecRecords = execRecordMapper.selectByExample(needExecCondition);
        needExecRecords.forEach(execRecord -> {
            LOGGER.debug("Submit record_id={}. exec_id={}, task_id={}.", execRecord.getRecordId(), execRecord.getExecId(), execRecord.getTaskId());
            threadPoolExecutor.execute(handlerFactory.handle(execRecord));
        });
        LOGGER.debug("threadPoolExecutor = {} ", threadPoolExecutor);
    }


    @Override
    public CommonResult ensure(int recordId, String result,String userName) {
        EmergencyExecRecordWithBLOBs needEnsureRecord = execRecordMapper.selectByPrimaryKey(recordId);
        if (needEnsureRecord == null || needEnsureRecord.getRecordId() == null) {
            return CommonResult.failed("请选择正确的子任务。");
        }
        if (!"3".equals(needEnsureRecord.getStatus())) {
            return CommonResult.failed("该子任务不处于执行失败，无需确认！");
        }

        EmergencyExecRecordWithBLOBs updateRecord = new EmergencyExecRecordWithBLOBs();
        updateRecord.setStatus(result);
        updateRecord.setRecordId(needEnsureRecord.getRecordId());
        updateRecord.setEnsureUser(userName);
        if (execRecordMapper.updateByPrimaryKeySelective(updateRecord) == 0) {
            return CommonResult.failed("确认失败！");
        }

        // 当前子任务完成，执行后续任务
        if ("5".equals(result)) {
            if (needEnsureRecord.getTaskId() != null) {
                taskService.onComplete(needEnsureRecord);
            } else {
                sceneService.onComplete(needEnsureRecord);
            }
        }
        if ("6".equals(result)) {
            stopOtherRecordsById(needEnsureRecord.getRecordId());
        }
        return CommonResult.success();
    }

    public void stopOtherRecordsById(int recordId) {
        EmergencyExecRecordWithBLOBs record = execRecordMapper.selectByPrimaryKey(recordId);

        EmergencyExecRecordWithBLOBs updateRecord = new EmergencyExecRecordWithBLOBs();
        updateRecord.setStatus("6");

        EmergencyExecRecordExample updateCondition = new EmergencyExecRecordExample();
        updateCondition.createCriteria()
            .andExecIdEqualTo(record.getExecId())
            .andIsValidEqualTo("1")
            .andStatusEqualTo("0");
        execRecordMapper.updateByExampleSelective(updateRecord, updateCondition);
    }

}
