/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.huawei.common.constant.RecordStatus;
import com.huawei.common.util.PasswordUtil;
import com.huawei.emergency.entity.EmergencyExecRecord;
import com.huawei.emergency.entity.EmergencyExecRecordDetail;
import com.huawei.emergency.entity.EmergencyExecRecordDetailExample;
import com.huawei.emergency.entity.EmergencyExecRecordExample;
import com.huawei.emergency.entity.EmergencyExecRecordWithBLOBs;
import com.huawei.emergency.mapper.EmergencyExecRecordDetailMapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * 工厂类,用于提供脚本运行记录处理器
 *
 * @author y30010171
 * @since 2021-11-04
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class ExecRecordHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecRecordHandlerFactory.class);

    private static Map<String, ThreadPoolExecutor> remoteServerThreadCache = new ConcurrentHashMap<>();

    @Value("${script.executor.maxSubtaskSize}")
    private int maxSubtaskSize;

    @Autowired
    private EmergencyTaskService taskService;

    @Autowired
    private EmergencySceneService sceneService;

    @Autowired
    private EmergencyExecRecordMapper recordMapper;

    @Autowired
    private EmergencyExecRecordDetailMapper recordDetailMapper;

    @Autowired
    private Map<String, ScriptExecutor> allScriptExecutors;

    @Resource(name = "passwordRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private PasswordUtil passwordUtil;

    public ThreadPoolExecutor getThreadPool(String threadName) {
        ThreadPoolExecutor threadPoolExecutor = remoteServerThreadCache.get(threadName);
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(
                maxSubtaskSize,
                maxSubtaskSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024),
                new ThreadFactory() {
                    private AtomicInteger threadCount = new AtomicInteger();

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, threadName + "-" + threadCount.getAndIncrement());
                    }
                });
            remoteServerThreadCache.put(threadName, threadPoolExecutor);
        }
        return threadPoolExecutor;
    }

    @PreDestroy
    public void shutdown() {
        remoteServerThreadCache.values().forEach(ThreadPoolExecutor::shutdown);
    }

    /**
     * 获取一个执行器实例
     *
     * @param currentRecord 需要执行的脚本记录
     * @return Runnable
     */
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
            EmergencyExecRecordWithBLOBs record = recordMapper.selectByPrimaryKey(currentRecord.getRecordId());

            // 出现事务还未提交，此时查不到这条数据
            int retryTimes = 10;
            while (record == null && retryTimes > 0) {
                record = recordMapper.selectByPrimaryKey(currentRecord.getRecordId());
                retryTimes--;
            }
            if (record == null || !RecordStatus.PENDING.getValue().equals(record.getStatus())) {
                return;
            }
            List<EmergencyExecRecordDetail> emergencyExecRecordDetails = generateRecordDetail(record);
            ThreadPoolExecutor threadPoolExecutor = getThreadPool(Thread.currentThread().getName());
            EmergencyExecRecordWithBLOBs finalRecord = record;
            emergencyExecRecordDetails.forEach(recordDetail -> {
                threadPoolExecutor.execute(new ExecRecordDetailHandler(finalRecord, recordDetail));
            });
        }
    }

    private class ExecRecordDetailHandler implements Runnable {
        private final EmergencyExecRecordWithBLOBs record;
        private final EmergencyExecRecordDetail recordDetail;

        private ExecRecordDetailHandler(EmergencyExecRecordWithBLOBs record, EmergencyExecRecordDetail recordDetail) {
            this.record = record;
            this.recordDetail = recordDetail;
        }

        @Override
        public void run() {
            ExecResult execResult = ExecResult.success("");
            EmergencyExecRecordWithBLOBs updateRecord = new EmergencyExecRecordWithBLOBs();
            updateRecord.setRecordId(recordDetail.getRecordId());
            EmergencyExecRecordDetail updateRecordDetail = new EmergencyExecRecordDetail();
            updateRecordDetail.setDetailId(recordDetail.getDetailId());
            try {
                // 更新record,detail为执行中，开始时间
                Date startTime = new Date();
                updateRecordDetail.setStartTime(startTime);
                updateRecordDetail.setStatus(RecordStatus.RUNNING.getValue());
                recordDetailMapper.updateByPrimaryKeySelective(updateRecordDetail);

                recordMapper.tryUpdateStartTime(updateRecord.getRecordId(), startTime);
                recordMapper.tryUpdateStatus(updateRecord.getRecordId());

                ScriptExecInfo execInfo = generateExecInfo(record, recordDetail);
                if (record.getScriptId() != null) {
                    ScriptExecutor scriptExecutor = execInfo.getRemoteServerInfo() == null
                        ? allScriptExecutors.get("localScriptExecutor")
                        : allScriptExecutors.get("remoteScriptExecutor");
                    execResult = scriptExecutor.execScript(execInfo, new DefaultLogCallBack(record.getRecordId()));
                }
            } catch (Exception e) {
                execResult = ExecResult.fail(e.getMessage());
            } finally {
                // 更新record,detail为执行完成，结束时间
                Date endTime = new Date();
                EmergencyExecRecordDetailExample whenRunning = new EmergencyExecRecordDetailExample();
                whenRunning.createCriteria()
                    .andDetailIdEqualTo(recordDetail.getDetailId())
                    .andIsValidEqualTo("1")
                    .andStatusEqualTo(RecordStatus.RUNNING.getValue());
                updateRecordDetail.setEndTime(endTime);
                updateRecordDetail.setLog(execResult.getMsg());
                updateRecordDetail.setStatus(execResult.isSuccess() ? RecordStatus.SUCCESS.getValue() : RecordStatus.ENSURE_FAILED.getValue());
                recordDetailMapper.updateByExampleSelective(updateRecordDetail, whenRunning); // 做个状态判断，防止人为取消 也被标记为执行成功

                recordMapper.tryUpdateEndTime(updateRecord.getRecordId(), endTime);
                recordMapper.tryUpdateStatus(updateRecord.getRecordId());

                // 清除实时日志的在内存中的日志残留
                LogMemoryStore.removeLog(recordDetail.getDetailId());

                // 回调
                if (execResult.isSuccess() && isRecordFinished(record.getRecordId())) {
                    if (record.getTaskId() != null) {
                        taskService.onComplete(record);
                    } else {
                        sceneService.onComplete(record);
                    }
                }
            }
        }
    }

    private ScriptExecInfo generateExecInfo(EmergencyExecRecordWithBLOBs record, EmergencyExecRecordDetail recordDetail) {
        ScriptExecInfo execInfo = new ScriptExecInfo();
        execInfo.setId(recordDetail.getDetailId());
        execInfo.setScriptName(record.getScriptName() + "-" + record.getRecordId());
        execInfo.setScriptContext(record.getScriptContent());
        if (StringUtils.isNotEmpty(record.getScriptParams())) {
            String[] split = record.getScriptParams().split(",");
            execInfo.setParams(split);
        }

        if (StringUtils.isNotEmpty(recordDetail.getServerIp())) {
            ServerInfo serverInfo = new ServerInfo(recordDetail.getServerIp(), record.getServerUser());
            if ("1".equals(record.getHavePassword())) {
                serverInfo.setServerPassword(parsePassword(record.getPasswordMode(), record.getPassword()));
            }
            execInfo.setRemoteServerInfo(serverInfo);
        }
        return execInfo;
    }

    private List<EmergencyExecRecordDetail> generateRecordDetail(EmergencyExecRecord record) {
        List<EmergencyExecRecordDetail> result = new ArrayList<>();
        if (StringUtils.isNotEmpty(record.getServerIp())) {
            String[] ipArr = record.getServerIp().split(",");
            for (String ip : ipArr) {
                EmergencyExecRecordDetail recordDetail = new EmergencyExecRecordDetail();
                recordDetail.setExecId(record.getExecId());
                recordDetail.setRecordId(record.getRecordId());
                recordDetail.setStatus(RecordStatus.PENDING.getValue());
                recordDetail.setServerIp(ip);
                recordDetailMapper.insertSelective(recordDetail);
                result.add(recordDetail);
            }
        } else {
            EmergencyExecRecordDetail recordDetail = new EmergencyExecRecordDetail();
            recordDetail.setExecId(record.getExecId());
            recordDetail.setRecordId(record.getRecordId());
            recordDetail.setStatus(RecordStatus.PENDING.getValue());
            recordDetailMapper.insertSelective(recordDetail);
            result.add(recordDetail);
        }
        return result;
    }

    private String parsePassword(String mode, String source) {
        if ("0".equals(mode)) {
            try {
                return passwordUtil.decodePassword(source);
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Decode password error.", e);
                return source;
            }
        }
        return restTemplate.getForObject(source, String.class);
    }

    private boolean isRecordFinished(int recordId) {
        EmergencyExecRecordDetailExample isFinished = new EmergencyExecRecordDetailExample();
        isFinished.createCriteria()
            .andRecordIdEqualTo(recordId)
            .andIsValidEqualTo("1")
            .andStatusIn(Arrays.asList("0", "1", "3", "4"));
        return recordDetailMapper.countByExample(isFinished) == 0;
    }
}
