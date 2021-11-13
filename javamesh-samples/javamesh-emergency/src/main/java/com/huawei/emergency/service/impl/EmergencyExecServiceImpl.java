/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.huawei.common.api.CommonResult;
import com.huawei.emergency.entity.EmergencyExec;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyExecRecordExample;
import com.huawei.emergency.entity.EmergencyExecRecordWithBLOBs;
import com.huawei.emergency.entity.EmergencyScript;
import com.huawei.emergency.mapper.EmergencyExecMapper;
import com.huawei.emergency.mapper.EmergencyExecRecordMapper;
import com.huawei.emergency.service.EmergencyExecService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    @Override
    public CommonResult exec(EmergencyScript script) {

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
        exec.setCreateUser("admin");
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
        record.setCreateUser("admin");
        recordMapper.insertSelective(record);

        threadPoolExecutor.execute(handlerFactory.handle(record));
        LOGGER.debug("threadPoolExecutor = {} ", threadPoolExecutor);

        EmergencyExecRecord result =new EmergencyExecRecord();
        result.setExecId(record.getExecId());
        result.setRecordId(record.getRecordId());
        result.setDetailId(record.getRecordId());
        return CommonResult.success(result);
    }

    @Override
    public String getLog(int recordId) {
        EmergencyExecRecordWithBLOBs record = recordMapper.selectByPrimaryKey(recordId);
        if (record == null){
            return "";
        }
        return record.getLog();
    }
}
