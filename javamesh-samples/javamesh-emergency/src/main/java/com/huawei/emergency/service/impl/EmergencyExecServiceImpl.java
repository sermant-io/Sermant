/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.PlanStatus;
import com.huawei.common.constant.RecordStatus;
import com.huawei.common.constant.ValidEnum;
import com.huawei.emergency.dto.PlanQueryDto;
import com.huawei.emergency.dto.SceneExecDto;
import com.huawei.emergency.entity.EmergencyExec;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyExecRecordDetail;
import com.huawei.emergency.entity.EmergencyExecRecordDetailExample;
import com.huawei.emergency.entity.EmergencyExecRecordExample;
import com.huawei.emergency.entity.EmergencyExecRecordWithBLOBs;
import com.huawei.emergency.entity.EmergencyPlan;
import com.huawei.emergency.entity.EmergencyScript;
import com.huawei.emergency.entity.EmergencyServer;
import com.huawei.emergency.entity.EmergencyServerExample;
import com.huawei.emergency.mapper.EmergencyExecMapper;
import com.huawei.emergency.mapper.EmergencyExecRecordDetailMapper;
import com.huawei.emergency.mapper.EmergencyExecRecordMapper;
import com.huawei.emergency.mapper.EmergencyPlanMapper;
import com.huawei.emergency.mapper.EmergencyServerMapper;
import com.huawei.emergency.service.EmergencyExecService;
import com.huawei.emergency.service.EmergencySceneService;
import com.huawei.emergency.service.EmergencyTaskService;
import com.huawei.script.exec.ExecResult;
import com.huawei.script.exec.executor.ScriptExecutor;
import com.huawei.script.exec.log.LogMemoryStore;
import com.huawei.script.exec.log.LogResponse;

import com.huawei.script.exec.session.ServerInfo;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@Setter
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
    private EmergencyExecRecordDetailMapper recordDetailMapper;

    @Autowired
    private EmergencySceneService sceneService;

    @Autowired
    private EmergencyTaskService taskService;

    @Autowired
    private EmergencyServerMapper serverMapper;

    @Autowired
    private EmergencyPlanMapper planMapper;

    @Autowired
    private Map<String, ScriptExecutor> scriptExecutors;

    @Override
    public CommonResult exec(EmergencyScript script) {
        if (script == null || script.getScriptId() == null) {
            return CommonResult.failed("请选择正确的脚本.");
        }

        // 是否运行
        EmergencyExecRecordExample isScriptRunning = new EmergencyExecRecordExample();
        isScriptRunning.createCriteria()
            .andScriptIdEqualTo(script.getScriptId())
            .andPlanIdEqualTo(0)
            .andSceneIdEqualTo(0)
            .andTaskIdEqualTo(0)
            .andIsValidEqualTo(ValidEnum.VALID.getValue())
            .andStatusIn(Arrays.asList(RecordStatus.PENDING.getValue(), RecordStatus.RUNNING.getValue()));
        if (recordMapper.countByExample(isScriptRunning) > 0) {
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
        record.setStatus(RecordStatus.PENDING.getValue());
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

        List<EmergencyExecRecordDetail> emergencyExecRecordDetails = handlerFactory.generateRecordDetail(record);
        emergencyExecRecordDetails.forEach(recordDetail -> {
            threadPoolExecutor.execute(handlerFactory.handleDetail(record, recordDetail));
        });

        EmergencyExecRecord result = new EmergencyExecRecord();
        result.setExecId(record.getExecId());
        result.setRecordId(record.getRecordId());
        result.setDebugId(emergencyExecRecordDetails.get(0).getDetailId());
        return CommonResult.success(result);
    }

    @Override
    public CommonResult debugScript(String content, String serverName) {
        if (StringUtils.isEmpty(content)) {
            return CommonResult.failed("脚本内容为空");
        }
        EmergencyExec exec = new EmergencyExec();
        exec.setCreateUser("system");
        execMapper.insertSelective(exec);

        EmergencyExecRecordWithBLOBs record = new EmergencyExecRecordWithBLOBs();
        record.setExecId(exec.getExecId());
        record.setPlanId(0);
        record.setSceneId(0);
        record.setTaskId(0);
        record.setStatus(RecordStatus.PENDING.getValue());
        record.setScriptName("debug");
        record.setScriptContent(content);
        record.setCreateUser("system");
        if (StringUtils.isNotEmpty(serverName)) {
            EmergencyServerExample isServerExist = new EmergencyServerExample();
            isServerExist.createCriteria()
                .andServerNameEqualTo(serverName)
                .andIsValidEqualTo(ValidEnum.VALID.getValue());
            List<EmergencyServer> serverList = serverMapper.selectByExample(isServerExist);
            if (serverList.size() == 0) {
                return CommonResult.failed("请选择正确的服务器");
            }
            record.setServerId(serverList.get(0).getServerId().toString());
        }
        recordMapper.insertSelective(record);

        List<EmergencyExecRecordDetail> emergencyExecRecordDetails = handlerFactory.generateRecordDetail(record);
        emergencyExecRecordDetails.forEach(recordDetail -> {
            threadPoolExecutor.execute(handlerFactory.handleDetail(record, recordDetail));
        });

        EmergencyExecRecord result = new EmergencyExecRecord();
        result.setExecId(record.getExecId());
        result.setRecordId(record.getRecordId());
        result.setDebugId(emergencyExecRecordDetails.get(0).getDetailId());
        return CommonResult.success(result);
    }

    @Override
    public LogResponse getLog(int detailId, int line) {
        /*String log = getLog(recordId);
        if (StringUtils.isEmpty(log)) {
            return LogMemoryStore.getLog(recordId, line);
        }
        String[] split = log.split(System.lineSeparator());
        if (split.length >= line) {
            String[] needLogs = Arrays.copyOfRange(split, line - 1, split.length);
            return new LogResponse(null, needLogs);
        }
        return new LogResponse(null, new String[]{log});*/
        return logOneServer(detailId, line);
    }

    @Override
    public LogResponse getRecordLog(int recordId, int line) {
        EmergencyExecRecordDetailExample recordDetailExample = new EmergencyExecRecordDetailExample();
        recordDetailExample.createCriteria()
            .andIsValidEqualTo(ValidEnum.VALID.getValue())
            .andRecordIdEqualTo(recordId);
        List<EmergencyExecRecordDetail> emergencyExecRecordDetails = recordDetailMapper.selectByExample(recordDetailExample);
        if (emergencyExecRecordDetails.size() == 0) {
            return LogResponse.END;
        }
        return logOneServer(emergencyExecRecordDetails.get(0).getDetailId(), line);
    }

    @Override
    public CommonResult ensure(int recordId, String result, String userName) {
        EmergencyExecRecordWithBLOBs needEnsureRecord = recordMapper.selectByPrimaryKey(recordId);
        if (needEnsureRecord == null || needEnsureRecord.getRecordId() == null) {
            return CommonResult.failed("请选择正确的子任务。");
        }
        if (!RecordStatus.FAILED.getValue().equals(needEnsureRecord.getStatus())) {
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
        if (RecordStatus.ENSURE_SUCCESS.getValue().equals(result)) {
            if (needEnsureRecord.getTaskId() != null) {
                taskService.onComplete(needEnsureRecord);
            } else {
                sceneService.onComplete(needEnsureRecord);
            }
        }
        if (RecordStatus.ENSURE_FAILED.getValue().equals(result)) {
            stopOtherRecordsById(needEnsureRecord.getRecordId());
            EmergencyPlan plan = new EmergencyPlan();
            plan.setPlanId(needEnsureRecord.getPlanId());
            plan.setStatus(PlanStatus.FAILED.getValue());
            planMapper.updateByPrimaryKeySelective(plan);
        }
        handlerFactory.notifySceneRefresh(needEnsureRecord.getExecId(), needEnsureRecord.getSceneId());
        return CommonResult.success();
    }

    public void stopOtherRecordsById(int recordId) {
        EmergencyExecRecordWithBLOBs record = recordMapper.selectByPrimaryKey(recordId);
        EmergencyExecRecordWithBLOBs updateRecord = new EmergencyExecRecordWithBLOBs();
        updateRecord.setStatus(RecordStatus.ENSURE_FAILED.getValue());
        EmergencyExecRecordExample updateCondition = new EmergencyExecRecordExample();
        updateCondition.createCriteria()
            .andExecIdEqualTo(record.getExecId())
            .andIsValidEqualTo(ValidEnum.VALID.getValue())
            .andStatusEqualTo(RecordStatus.PENDING.getValue());
        recordMapper.updateByExampleSelective(updateRecord, updateCondition);
    }

    @Override
    public CommonResult reExec(int recordId, String userName) {
        EmergencyExecRecordWithBLOBs oldRecord = recordMapper.selectByPrimaryKey(recordId);
        if (!RecordStatus.FAILED.getValue().equals(oldRecord.getStatus()) ||
            !ValidEnum.VALID.getValue().equals(oldRecord.getIsValid())) {
            return CommonResult.failed("请选择执行失败的执行记录");
        }

        EmergencyExecRecordWithBLOBs updateRecord = new EmergencyExecRecordWithBLOBs();
        updateRecord.setRecordId(oldRecord.getRecordId());
        updateRecord.setIsValid(ValidEnum.IN_VALID.getValue());
        recordMapper.updateByPrimaryKeySelective(updateRecord);

        oldRecord.setCreateUser(userName);
        oldRecord.setCreateTime(new Date());
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

    @Override
    public CommonResult stopOneServer(int detailId, String userName) {
        EmergencyExecRecordDetail recordDetail = recordDetailMapper.selectByPrimaryKey(detailId);
        if (recordDetail == null
            || ValidEnum.IN_VALID.getValue().equals(recordDetail.getIsValid())
            || !RecordStatus.RUNNING.getValue().equals(recordDetail.getStatus())
            || recordDetail.getPid() == null) {
            return CommonResult.failed("请选择正在执行中的记录");
        }
        ExecResult cancelResult;
        if (recordDetail.getServerId() == null) {
            cancelResult = scriptExecutors.get("localScriptExecutor").cancel(null, recordDetail.getPid());
        } else {
            EmergencyServer server = serverMapper.selectByPrimaryKey(recordDetail.getServerId());
            if (server == null) {
                return CommonResult.failed("获取服务器信息失败");
            }
            ServerInfo serverInfo = new ServerInfo(server.getServerIp(), server.getServerUser(), server.getServerPort());
            if ("1".equals(server.getHavePassword())) {
                serverInfo.setServerPassword(handlerFactory.parsePassword(server.getPasswordMode(), server.getPassword()));
            }
            cancelResult = scriptExecutors.get("remoteScriptExecutor").cancel(serverInfo, recordDetail.getPid());
        }
        if (!cancelResult.isSuccess()) {
            return CommonResult.failed(cancelResult.getMsg());
        }

        EmergencyExecRecordDetail updateDetail = new EmergencyExecRecordDetail();
        updateDetail.setDetailId(detailId);
        updateDetail.setStatus(RecordStatus.CANCEL.getValue());
        updateDetail.setEndTime(new Date());
        LogResponse logResponse = LogMemoryStore.getLog(detailId, 1);
        StringBuilder logBuilder = new StringBuilder();
        for (String log : logResponse.getData()) {
            logBuilder.append(log);
        }
        logBuilder.append(userName).append("于").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("取消执行");
        updateDetail.setLog(logBuilder.toString());
        recordDetailMapper.updateByPrimaryKeySelective(updateDetail);
        return CommonResult.success();
    }

    @Override
    public CommonResult startOneServer(int detailId, String userName) {
        EmergencyExecRecordDetail oldDetail = recordDetailMapper.selectByPrimaryKey(detailId);
        if (oldDetail == null
            || ValidEnum.IN_VALID.getValue().equals(oldDetail.getIsValid())) {
            return CommonResult.failed("请选择正在执行中的记录");
        }
        if (!RecordStatus.FAILED.getValue().equals(oldDetail.getStatus()) && !RecordStatus.CANCEL.getValue().equals(oldDetail.getStatus())) {
            return CommonResult.failed("请选择执行失败或取消的记录");
        }
        EmergencyExecRecordWithBLOBs record = recordMapper.selectByPrimaryKey(oldDetail.getRecordId());
        if (record == null || ValidEnum.IN_VALID.equals(record.getIsValid())) {
            return CommonResult.failed("该任务的执行记录不存在");
        }

        EmergencyExecRecordDetail updateDetail = new EmergencyExecRecordDetail();
        updateDetail.setDetailId(detailId);
        updateDetail.setIsValid(ValidEnum.IN_VALID.getValue());
        recordDetailMapper.updateByPrimaryKeySelective(updateDetail);

        oldDetail.setDetailId(null);
        oldDetail.setCreateUser(userName);
        oldDetail.setCreateTime(new Date());
        oldDetail.setStartTime(null);
        oldDetail.setEndTime(null);
        oldDetail.setEnsureUser(null);
        oldDetail.setEnsureTime(null);
        oldDetail.setLog(null);
        oldDetail.setPid(null);
        oldDetail.setStatus(RecordStatus.PENDING.getValue());
        recordDetailMapper.insertSelective(oldDetail);
        threadPoolExecutor.execute(handlerFactory.handleDetail(record, oldDetail));
        return CommonResult.success(oldDetail);
    }

    @Override
    public CommonResult ensureOneServer(int detailId, String result, String userName) {
        EmergencyExecRecordDetail recordDetail = recordDetailMapper.selectByPrimaryKey(detailId);
        if (recordDetail == null
            || ValidEnum.IN_VALID.getValue().equals(recordDetail.getIsValid())
            || !RecordStatus.FAILED.getValue().equals(recordDetail.getStatus())) {
            return CommonResult.failed("请选择执行失败的记录");
        }
        EmergencyExecRecordWithBLOBs record = recordMapper.selectByPrimaryKey(recordDetail.getRecordId());
        if (record == null || ValidEnum.IN_VALID.equals(record.getIsValid())) {
            return CommonResult.failed("该任务的执行记录不存在");
        }

        EmergencyExecRecordDetail updateDetail = new EmergencyExecRecordDetail();
        updateDetail.setStatus(result);
        updateDetail.setDetailId(detailId);
        updateDetail.setEnsureUser(userName);
        updateDetail.setEnsureTime(new Date());
        recordDetailMapper.updateByPrimaryKeySelective(updateDetail);

        // todo 更新一次record的状态，并判断当前record是否完成
        recordMapper.tryUpdateStatus(recordDetail.getRecordId());
        record = recordMapper.selectByPrimaryKey(recordDetail.getRecordId());
        if (RecordStatus.SUCCESS.getValue().equals(record.getStatus())) {
            if (record.getTaskId() != null) {
                taskService.onComplete(record);
            } else {
                sceneService.onComplete(record);
            }
        }
        return CommonResult.success();
    }

    @Override
    public LogResponse logOneServer(int detailId, int line) {
        EmergencyExecRecordDetail recordDetail = recordDetailMapper.selectByPrimaryKey(detailId);
        if (recordDetail == null || ValidEnum.IN_VALID.equals(recordDetail.getIsValid())) {
            return LogResponse.END;
        }
        if (StringUtils.isEmpty(recordDetail.getLog())) {
            LogResponse log = LogMemoryStore.getLog(detailId, line);
            if (log.getLine() == null && log.getData().length == 0) { // 可能开始执行 但还未生成日志
                return new LogResponse(line, LogMemoryStore.EMPTY_ARRAY);
            }
            return log;
        }
        String[] split = recordDetail.getLog().split(System.lineSeparator());
        if (split.length >= line) {
            String[] needLogs = Arrays.copyOfRange(split, line - 1, split.length);
            return new LogResponse(null, needLogs);
        }
        return new LogResponse(null, new String[]{recordDetail.getLog()});
        /*EmergencyExecRecordDetail recordDetail = recordDetailMapper.selectByPrimaryKey(detailId);
        if (recordDetail == null || ValidEnum.IN_VALID.equals(recordDetail.getIsValid())) {
            return LogResponse.END;
        }
        if (StringUtils.isEmpty(recordDetail.getLog())) {
            return LogMemoryStore.getLog(detailId, line);
        }
        String[] split = recordDetail.getLog().split(System.lineSeparator());
        if (split.length >= line) {
            String[] needLogs = Arrays.copyOfRange(split, line - 1, split.length);
            return new LogResponse(null, needLogs);
        }
        return new LogResponse(null, new String[]{recordDetail.getLog()});*/
    }

    @Override
    public CommonResult allPlanExecRecords(CommonPage<EmergencyPlan> params, String[] filterPlanNames, String[] filterCreators) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("planNames", filterPlanNames);
        filters.put("creators", filterCreators);
        Page<PlanQueryDto> pageInfo = PageHelper
            .startPage(params.getPageIndex(), params.getPageSize(), StringUtils.isEmpty(params.getSortType()) ? "" : params.getSortField() + System.lineSeparator() + params.getSortType())
            .doSelectPage(() -> {
                execMapper.allPlanRecords(params.getObject(), filters);
            });
        return CommonResult.success(pageInfo.getResult(), (int) pageInfo.getTotal());
    }

    @Override
    public CommonResult allSceneExecRecords(CommonPage<EmergencyExecRecord> params) {
        List<SceneExecDto> sceneExecDtos = execMapper.allSceneRecords(params.getObject().getExecId());
        return CommonResult.success(sceneExecDtos, sceneExecDtos.size());
    }

    @Override
    public CommonResult allTaskExecRecords(CommonPage<EmergencyExecRecord> params) {
        EmergencyExecRecord paramsObject = params.getObject();
        List<SceneExecDto> result = execMapper.allTaskRecords(paramsObject.getExecId(), paramsObject.getSceneId());
        result.forEach(recordDto -> {
            recordDto.setScheduleInfo(recordDetailMapper.selectAllServerDetail(recordDto.getKey()));
        });
        return CommonResult.success(result, result.size());
    }
}
