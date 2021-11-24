/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.RecordStatus;
import com.huawei.emergency.entity.EmergencyExec;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyExecRecordExample;
import com.huawei.emergency.entity.EmergencyExecRecordWithBLOBs;
import com.huawei.emergency.entity.EmergencyScript;
import com.huawei.emergency.mapper.EmergencyExecMapper;
import com.huawei.emergency.mapper.EmergencyExecRecordMapper;
import com.huawei.emergency.service.EmergencyExecService;

import com.huawei.emergency.service.EmergencySceneService;
import com.huawei.emergency.service.EmergencyTaskService;
import com.huawei.script.exec.log.LogMemoryStore;
import com.huawei.script.exec.log.LogResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Resource;

/**
 * 执行记录管理
 *
 * @author y30010171
 * @since 2021-11-09
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class EmergencyExecServiceImpl implements EmergencyExecService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmergencyExecServiceImpl.class);

    @Resource(name = "scriptExecThreadPool")
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private ExecRecordHandlerFactory handlerFactory;

    @Autowired
    private EmergencyExecMapper execMapper;

    @Autowired
    private EmergencyExecRecordMapper recordMapper;

    @Autowired
    private EmergencySceneService sceneService;

    @Autowired
    private EmergencyTaskService taskService;

    @Override
    public CommonResult exec(EmergencyScript script) {
        if (script == null || script.getScriptId() == null){
            return CommonResult.failed("请选择正确的脚本.");
        }

        // 是否运行
        EmergencyExecRecordExample isScriptRunning = new EmergencyExecRecordExample();
        isScriptRunning.createCriteria()
            .andScriptIdEqualTo(script.getScriptId())
            .andPlanIdEqualTo(0)
            .andSceneIdEqualTo(0)
            .andTaskIdEqualTo(0)
            .andIsValidEqualTo("1")
            .andStatusIn(Arrays.asList("0","1"));
        if (recordMapper.countByExample(isScriptRunning) > 0){
            return CommonResult.failed("脚本正在调试中");
        }

        // 增加执行记录
        EmergencyExec exec = new EmergencyExec();
        exec.setCreateUser("system");
        exec.setScriptId(script.getScriptId());
        execMapper.insertSelective(exec);

        // 增加执行明细 BeanUtils.copyProperties(script,record);
        EmergencyExecRecordWithBLOBs record = new EmergencyExecRecordWithBLOBs();
        record.setExecId(exec.getExecId());
        record.setPlanId(0);
        record.setSceneId(0);
        record.setTaskId(0);
        record.setStatus("0");
        record.setScriptId(script.getScriptId());
        record.setScriptName(script.getScriptName());
        record.setScriptContent(script.getContent());
        record.setScriptType(script.getScriptType());
        record.setScriptParams(script.getParam());
        record.setServerIp(script.getServerIp());
        record.setServerUser(script.getServerUser());
        record.setHavePassword(script.getHavePassword());
        record.setPasswordMode(script.getPasswordMode());
        record.setPassword(script.getPassword());
        record.setCreateUser("system");
        recordMapper.insertSelective(record);

        threadPoolExecutor.execute(handlerFactory.handle(record));
        LOGGER.debug("threadPoolExecutor = {} ", threadPoolExecutor);

        EmergencyExecRecord result =new EmergencyExecRecord();
        result.setExecId(record.getExecId());
        result.setRecordId(record.getRecordId());
        result.setDebugId(record.getRecordId());
        return CommonResult.success(result);
    }

    private String getLog(int recordId) {
        EmergencyExecRecordWithBLOBs record = recordMapper.selectByPrimaryKey(recordId);
        if (record == null){
            return "";
        }
        return record.getLog();
    }

    @Override
    public LogResponse getLog(int recordId, int line) {
        String log = getLog(recordId);
        if (StringUtils.isEmpty(log)) {
            return LogMemoryStore.getLog(recordId, line);
        }
        String[] split = log.split(System.lineSeparator());
        if (split.length >= line) {
            String[] needLogs = Arrays.copyOfRange(split, line - 1, split.length);
            return new LogResponse(null, needLogs);
        }
        return new LogResponse(null, new String[]{log});
    }

    @Override
    public CommonResult ensure(int recordId, String result,String userName) {
        EmergencyExecRecordWithBLOBs needEnsureRecord = recordMapper.selectByPrimaryKey(recordId);
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
        if (recordMapper.updateByPrimaryKeySelective(updateRecord) == 0) {
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
        EmergencyExecRecordWithBLOBs record = recordMapper.selectByPrimaryKey(recordId);
        EmergencyExecRecordWithBLOBs updateRecord = new EmergencyExecRecordWithBLOBs();
        updateRecord.setStatus("6");
        EmergencyExecRecordExample updateCondition = new EmergencyExecRecordExample();
        updateCondition.createCriteria()
            .andExecIdEqualTo(record.getExecId())
            .andIsValidEqualTo("1")
            .andStatusEqualTo("0");
        recordMapper.updateByExampleSelective(updateRecord, updateCondition);
    }

    @Override
    public CommonResult reExec(int recordId,String userName) {
        EmergencyExecRecordWithBLOBs oldRecord = recordMapper.selectByPrimaryKey(recordId);
        if (!RecordStatus.FAILED.getValue().equals(oldRecord.getStatus()) || !"1".equals(oldRecord.getIsValid())) {
            return CommonResult.failed("请选择执行失败的执行记录");
        }

        EmergencyExecRecordWithBLOBs updateRecord = new EmergencyExecRecordWithBLOBs();
        updateRecord.setRecordId(oldRecord.getRecordId());
        updateRecord.setIsValid("0");
        recordMapper.updateByPrimaryKeySelective(updateRecord);

        oldRecord.setCreateUser(userName);
        oldRecord.setCreateTime(null);
        oldRecord.setStartTime(null);
        oldRecord.setEndTime(null);
        oldRecord.setEnsureUser(null);
        oldRecord.setRecordId(null);
        oldRecord.setLog(null);
        oldRecord.setStatus(RecordStatus.PENDING.getValue());
        recordMapper.insertSelective(oldRecord);
        threadPoolExecutor.execute(handlerFactory.handle(oldRecord));
        return CommonResult.success(oldRecord);
    }
}
