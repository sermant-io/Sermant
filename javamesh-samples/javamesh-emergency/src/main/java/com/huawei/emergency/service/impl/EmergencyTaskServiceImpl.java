/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.huawei.common.api.CommonResult;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyExecRecordExample;
import com.huawei.emergency.entity.EmergencyScript;
import com.huawei.emergency.entity.EmergencyScriptExample;
import com.huawei.emergency.entity.EmergencyTask;
import com.huawei.emergency.entity.EmergencyTaskExample;
import com.huawei.emergency.mapper.EmergencyExecRecordMapper;
import com.huawei.emergency.mapper.EmergencyScriptMapper;
import com.huawei.emergency.mapper.EmergencyTaskMapper;
import com.huawei.emergency.service.EmergencySceneService;
import com.huawei.emergency.service.EmergencyTaskService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Resource;

/**
 * 任务管理接口实现类
 *
 * @author y30010171
 * @since 2021-11-04
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class EmergencyTaskServiceImpl implements EmergencyTaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmergencyTaskServiceImpl.class);


    @Resource(name = "scriptExecThreadPool")
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private EmergencyTaskMapper taskMapper;

    @Autowired
    private EmergencyExecRecordMapper execRecordMapper;

    @Autowired
    private EmergencyScriptMapper scriptMapper;

    @Autowired
    private EmergencySceneService sceneService;

    @Autowired
    private ExecRecordHandlerFactory handlerFactory;

    @Override
    public void onComplete(EmergencyExecRecord record) {
        LOGGER.debug("Task exec_id={},plan_id={},scene_id={},task_id={} is finished.", record.getExecId(), record.getPlanId(), record.getSceneId(), record.getTaskId());

        // 如果场景已经完成
        if (sceneService.isSceneFinished(record.getExecId(), record.getSceneId())) {
            sceneService.onComplete(record);
            return;
        }

        if (isTaskFinished(record)) {
            // 执行依赖此任务的任务
            EmergencyExecRecordExample needExecTaskCondition = new EmergencyExecRecordExample();
            needExecTaskCondition.createCriteria()
                .andExecIdEqualTo(record.getExecId())
                .andPreTaskIdEqualTo(record.getTaskId())
                .andIsValidEqualTo("1")
                .andStatusEqualTo("0");
            List<EmergencyExecRecord> needExecTasks = execRecordMapper.selectByExample(needExecTaskCondition);
            needExecTasks.forEach(execRecord -> {
                LOGGER.debug("Submit record_id={}. exec_id={}, task_id={}.", execRecord.getRecordId(), execRecord.getExecId(), execRecord.getTaskId());
                threadPoolExecutor.execute(handlerFactory.handle(execRecord));
            });

            if (needExecTasks.size() == 0) {

                if (record.getParentTaskId().equals(record.getSceneId())) {
                    // 父任务是场景
                    EmergencyExecRecordExample parentRecordCondition = new EmergencyExecRecordExample();
                    parentRecordCondition.createCriteria()
                        .andExecIdEqualTo(record.getExecId())
                        .andIsValidEqualTo("1")
                        .andSceneIdEqualTo(record.getSceneId())
                        .andTaskIdIsNull();
                    List<EmergencyExecRecord> parentRecords = execRecordMapper.selectByExample(parentRecordCondition);
                    parentRecords.forEach(sceneService::onComplete);
                } else {
                    // 通知父任务
                    EmergencyExecRecordExample parentRecordCondition = new EmergencyExecRecordExample();
                    parentRecordCondition.createCriteria()
                        .andExecIdEqualTo(record.getExecId())
                        .andIsValidEqualTo("1")
                        .andTaskIdEqualTo(record.getParentTaskId());
                    List<EmergencyExecRecord> parentRecords = execRecordMapper.selectByExample(parentRecordCondition);
                    parentRecords.forEach(this::onComplete);
                }
            }
        } else {
            // 执行子任务
            EmergencyExecRecordExample subTask = new EmergencyExecRecordExample();
            subTask.createCriteria()
                .andExecIdEqualTo(record.getExecId())
                .andIsValidEqualTo("1")
                .andStatusEqualTo("0")
                .andPreTaskIdIsNull()
                .andParentTaskIdEqualTo(record.getTaskId());
            List<EmergencyExecRecord> emergencyExecRecords = execRecordMapper.selectByExample(subTask);
            emergencyExecRecords.forEach(execRecord -> {
                LOGGER.debug("Submit record_id={}. exec_id={}, task_id={}, task_detail_id={}.", execRecord.getRecordId(), execRecord.getExecId(), execRecord.getTaskId());
                threadPoolExecutor.execute(handlerFactory.handle(execRecord));
            });
        }
        LOGGER.debug("threadPoolExecutor = {} ", threadPoolExecutor);
    }

    public boolean isTaskFinished(EmergencyExecRecord task) {
        EmergencyExecRecordExample finishedCondition = new EmergencyExecRecordExample();
        finishedCondition.createCriteria()
            .andExecIdEqualTo(task.getExecId())
            .andParentTaskIdEqualTo(task.getTaskId())
            .andIsValidEqualTo("1");
        List<EmergencyExecRecord> emergencyExecRecords = execRecordMapper.selectByExample(finishedCondition);
        long runningCount = emergencyExecRecords.stream()
            .filter(record -> "0".equals(record.getStatus()) || "1".equals(record.getStatus()) || "3".equals(record.getStatus()) || "4".equals(record.getStatus()))
            .count();
        if (runningCount > 0L) {
            return false;
        }
        for (EmergencyExecRecord subTask : emergencyExecRecords) {
            if (!isTaskFinished(subTask)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public CommonResult<EmergencyTask> add(EmergencyTask emergencyTask) {
        if (StringUtils.isEmpty(emergencyTask.getTaskName())) {
            return CommonResult.failed("请填写任务名称");
        }

        EmergencyTask insertTask = new EmergencyTask();

        if (StringUtils.isNotEmpty(emergencyTask.getScriptName())) {
            EmergencyScriptExample scriptExample = new EmergencyScriptExample();
            scriptExample.createCriteria().andScriptNameEqualTo(emergencyTask.getScriptName());
            final List<EmergencyScript> emergencyScripts = scriptMapper.selectByExample(scriptExample);
            if (emergencyScripts.size() > 0) {
                insertTask.setScriptId(emergencyScripts.get(0).getScriptId());
                insertTask.setSubmitInfo(emergencyScripts.get(0).getSubmitInfo());
                insertTask.setScriptName(emergencyScripts.get(0).getScriptName());
            }
        }
        insertTask.setTaskName(emergencyTask.getTaskName());
        insertTask.setChannelType(emergencyTask.getChannelType());
        insertTask.setCreateUser(emergencyTask.getCreateUser());
        if (emergencyTask.getScriptId() != null) {
            EmergencyScript script = scriptMapper.selectByPrimaryKey(emergencyTask.getScriptId());
            if (script != null) {
                insertTask.setSubmitInfo(script.getSubmitInfo());
                insertTask.setScriptName(script.getScriptName());
            }
        }
        taskMapper.insertSelective(insertTask);

        EmergencyTask updateTaskNo = new EmergencyTask();
        updateTaskNo.setTaskId(insertTask.getTaskId());
        updateTaskNo.setTaskNo(String.format("%s%s%04d", "T", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE), insertTask.getTaskId()));
        taskMapper.updateByPrimaryKeySelective(updateTaskNo);
        insertTask.setTaskNo(updateTaskNo.getTaskNo());
        return CommonResult.success(insertTask);
    }

    @Override
    public CommonResult delete(EmergencyTask emergencyTask) {
        if (emergencyTask.getTaskId() == null) {
            return CommonResult.failed("请选择正确的任务");
        }

        EmergencyTask updateTask = new EmergencyTask();
        updateTask.setIsValid("0");
        updateTask.setTaskId(emergencyTask.getTaskId());
        if (taskMapper.updateByPrimaryKeySelective(updateTask) == 0) {
            return CommonResult.failed("删除失败");
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult update(EmergencyTask emergencyTask) {
        if (emergencyTask.getTaskId() == null) {
            return CommonResult.failed("请选择正确的任务");
        }

        // 验证预案是否已经通过审核
        if (taskMapper.countPassedPlanByTaskId(emergencyTask.getTaskId()) > 0) {
            return CommonResult.failed("无法操作已经审核通过的预案");
        }

        EmergencyTask updateTask = new EmergencyTask();
        updateTask.setTaskNo(emergencyTask.getTaskNo());
        updateTask.setTaskName(emergencyTask.getTaskName());
        updateTask.setTaskId(emergencyTask.getTaskId());
        if (taskMapper.updateByPrimaryKeySelective(updateTask) == 0) {
            return CommonResult.failed("修改失败");
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult bind(EmergencyTask task) {
        if (task.getTaskId() == null ||
            task.getPreTaskId() == null ||
            task.getPreTaskId() == task.getTaskId()) {
            CommonResult.failed("请选择两个不同场景进行绑定");
        }

        // 验证预案是否已经通过审核
        if (taskMapper.countPassedPlanByTaskId(task.getTaskId()) > 0) {
            return CommonResult.failed("无法操作已经审核通过的预案");
        }

        // 验证被依赖的任务是否有效
        EmergencyTaskExample preTaskExist = new EmergencyTaskExample();
        preTaskExist.createCriteria()
            .andTaskIdEqualTo(task.getPreTaskId())
            .andIsValidEqualTo("1");
        List<EmergencyTask> preTask = taskMapper.selectByExample(preTaskExist);
        if (preTask.size() == 0) {
            return CommonResult.failed("绑定失败");
        }

        // 将被依赖的任务与当前任务关联，同时确保是同一个场景下的任务
        EmergencyTask currentTask = new EmergencyTask();
        currentTask.setPreTaskId(task.getPreTaskId());
        EmergencyTaskExample currentSceneCondition = new EmergencyTaskExample();
        currentSceneCondition.createCriteria()
            .andTaskIdEqualTo(task.getTaskId())
            .andSceneIdEqualTo(preTask.get(0).getSceneId())
            .andIsValidEqualTo("1");
        if (taskMapper.updateByExampleSelective(currentTask, currentSceneCondition) == 0) {
            return CommonResult.failed("绑定失败");
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult unBind(EmergencyTask task) {
        if (task.getTaskId() == null ||
            task.getPreTaskId() == null) {
            CommonResult.failed("请选择两个不同的场景解除绑定");
        }

        // 验证预案是否已经通过审核
        if (taskMapper.countPassedPlanByTaskId(task.getTaskId()) > 0) {
            return CommonResult.failed("无法操作已经审核通过的预案");
        }

        // 验证当前任务与被依赖的任务的关系
        EmergencyTaskExample isTaskExist = new EmergencyTaskExample();
        isTaskExist.createCriteria()
            .andTaskIdEqualTo(task.getTaskId())
            .andPreTaskIdEqualTo(task.getPreTaskId())
            .andIsValidEqualTo("1");
        List<EmergencyTask> currentTask = taskMapper.selectByExample(isTaskExist);
        if (currentTask.size() == 0) {
            return CommonResult.failed("解除失败");
        }

        // 将被依赖的任务与当前任务解除关联
        currentTask.get(0).setPreTaskId(null);
        if (taskMapper.updateByPrimaryKeySelective(currentTask.get(0)) == 0) {
            return CommonResult.failed("解除失败");
        }
        return CommonResult.success();
    }

    @Override
    public boolean isTaskExist(int taskId) {
        EmergencyTaskExample existCondition = new EmergencyTaskExample();
        existCondition.createCriteria()
            .andTaskIdEqualTo(taskId)
            .andIsValidEqualTo("1");
        return taskMapper.countByExample(existCondition) > 0;
    }
}
