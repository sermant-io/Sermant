/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyExecRecordExample;
import com.huawei.emergency.entity.EmergencyExecRecordWithBLOBs;
import com.huawei.emergency.mapper.EmergencyExecRecordMapper;
import com.huawei.emergency.service.EmergencySceneService;
import com.huawei.emergency.service.EmergencyTaskService;
import com.huawei.script.exec.ExecResult;
import com.huawei.script.exec.executor.ScriptExecInfo;
import com.huawei.script.exec.executor.ScriptExecutor;
import com.huawei.script.exec.log.DefaultLogCallBack;
import com.huawei.script.exec.log.LogMemoryStore;
import com.huawei.script.exec.session.ServerInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;
import javax.annotation.Resource;

/**
 * 工厂类,用于提供脚本运行记录处理器
 *
 * @author y30010171
 * @since 2021-11-04
 **/
@Service
public class ExecRecordHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmergencyPlanServiceImpl.class);

    @Autowired
    private EmergencyTaskService taskService;

    @Autowired
    private EmergencySceneService sceneService;

    @Autowired
    private EmergencyExecRecordMapper execRecordMapper;

    @Autowired
    private Map<String, ScriptExecutor> allScriptExecutors;

    @Resource(name = "passwordRestTemplate")
    private RestTemplate restTemplate;

    public Runnable handle(EmergencyExecRecord currentRecord) {
        return new ExecRecordHandler(currentRecord);
    }

    /**
     * 脚本执行器
     *
     * @author y30010171
     * @since 2021-11-04
     **/
    private class ExecRecordHandler implements Runnable {

        private final EmergencyExecRecord currentRecord;

        ExecRecordHandler(EmergencyExecRecord currentRecord) {
            this.currentRecord = currentRecord;
        }

        @Override
        public void run() {
            EmergencyExecRecordWithBLOBs record = execRecordMapper.selectByPrimaryKey(currentRecord.getRecordId());

            // 出现事务还未提交，此时查不到这条数据
            int retryTimes = 10;
            while (record == null && retryTimes > 0) {
                record = execRecordMapper.selectByPrimaryKey(currentRecord.getRecordId());
                retryTimes--;
            }
            if (record == null || !"0".equals(record.getStatus())) {
                return;
            }
            ExecResult execResult = ExecResult.success("");
            try {
                record.setStartTime(new Date());
                record.setStatus("1");
                execRecordMapper.updateByPrimaryKeySelective(record);
                if (record.getScriptId() != null) {
                    execResult = allScriptExecutors.get("localScriptExecutor").execScript(generateExecInfo(record), new DefaultLogCallBack(record.getRecordId()));
                }
            } catch (Exception e) {
                execResult = ExecResult.fail(e.getMessage());
            } finally {
                // 更新为执行完成
                record.setEndTime(new Date());
                record.setLog(execResult.getMsg());
                record.setStatus(execResult.isSuccess() ? "2" : "3");
                EmergencyExecRecordExample updateCondition = new EmergencyExecRecordExample();
                updateCondition.createCriteria()
                    .andRecordIdEqualTo(record.getRecordId())
                    .andStatusEqualTo("1"); // 做个状态判断，防止人为取消 也被标记为执行成功
                execRecordMapper.updateByExampleSelective(record, updateCondition);

                LOGGER.info("exec script {} cost {} ms", record.getScriptName(), record.getEndTime().getTime() - record.getStartTime().getTime());

                // 清除实时日志的在内存中的日志残留
                LogMemoryStore.removeLog(record.getRecordId());
                if (execResult.isSuccess()) {
                    // detailService.onComplete(record);
                    if(record.getTaskId() == null){
                        sceneService.onComplete(record);
                    } else{
                        taskService.onComplete(record);
                    }
                }
            }
        }

        private ScriptExecInfo generateExecInfo(EmergencyExecRecordWithBLOBs record) {
            ScriptExecInfo execInfo = new ScriptExecInfo();
            execInfo.setId(record.getRecordId());
            execInfo.setScriptName(record.getScriptName() + "-" + record.getRecordId());
            execInfo.setScriptContext(record.getScriptContent());
            if (StringUtils.isNotEmpty(record.getServerIp())) {
                ServerInfo serverInfo = new ServerInfo(record.getServerIp(), record.getServerUser());
                if ("1".equals(record.getHavePassword())) {
                    serverInfo.setServerPassword(parsePassword(record.getPasswordMode(), record.getPassword()));
                }
                execInfo.setRemoteServerInfo(serverInfo);
            }
            return execInfo;
        }

        private String parsePassword(String mode, String source) {
            if ("0".equals(mode)) {
                return source;
            }
            return restTemplate.getForObject(source, String.class);
        }
    }
}
