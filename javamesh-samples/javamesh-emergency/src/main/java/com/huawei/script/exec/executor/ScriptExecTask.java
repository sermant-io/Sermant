/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.executor;

import com.huawei.emergency.entity.HistoryDetailEntity;
import com.huawei.script.exec.ExecResult;
import com.huawei.script.exec.log.DefaultLogCallBack;
import com.huawei.script.exec.session.ServerInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 脚本执行任务
 *
 * @author y30010171
 * @since 2021-10-26
 **/
public class ScriptExecTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptExecTask.class);
    /**
     * 待执行的脚本信息
     */
    private List<HistoryDetailEntity> allScriptInfo;
    /**
     * 可供选择的脚本执行器
     * <p>{@link LocalScriptExecutor} 本地执行</p>
     * <p>{@link RemoteScriptExecutor} 远程执行</p>
     */
    private Map<String, ScriptExecutor> scriptExecutorMap;

    /**
     * 脚本执行的回调接口
     */
    private ScriptCallBack scriptCallBack;

    public ScriptExecTask(List<HistoryDetailEntity> scriptInfo, Map<String, ScriptExecutor> scriptExecutorMap,
                          ScriptCallBack scriptCallBack) {
        this.allScriptInfo = scriptInfo;
        this.scriptExecutorMap = scriptExecutorMap;
        this.scriptCallBack = scriptCallBack == null ? new EmptyScriptCallBack() : scriptCallBack;
    }

    @Override
    public void run() {
        allScriptInfo.forEach(scriptInfo -> {
            ScriptExecutor executor = scriptExecutorMap.values()
                .stream()
                .filter(scriptExecutor -> scriptExecutor.mode().equals(String.valueOf(scriptInfo.getExecutionMode())))
                .findFirst()
                .get();
            ExecResult execResult = null;
            try {
                scriptCallBack.before(scriptInfo);
                execResult = executor.execScript(getExecInfo(scriptInfo), new DefaultLogCallBack(scriptInfo.getId()));
            } catch (Exception e) {
                LOGGER.error("Failed to exec task {}. {}", scriptInfo.getId(), e.getMessage());
                execResult = ExecResult.fail(e.getMessage());
            } finally {
                scriptCallBack.after(scriptInfo, execResult);
            }
        });
    }

    private ScriptExecInfo getExecInfo(HistoryDetailEntity scriptInfo) {
        ScriptExecInfo execInfo = new ScriptExecInfo();
        execInfo.setId(scriptInfo.getId());
        execInfo.setScriptName("script");
        execInfo.setScriptContext(scriptInfo.getContext());
        if (scriptInfo.getExecutionMode() == 1) {
            execInfo.setRemoteServerInfo(new ServerInfo(
                scriptInfo.getServerIp(), scriptInfo.getServerUser(), Integer.valueOf(scriptInfo.getServerPort())));
        }
        return execInfo;
    }

    /**
     * 空的回调函数
     *
     * @author y30010171
     * @since 2021-10-26
     */
    class EmptyScriptCallBack implements ScriptCallBack {
        @Override
        public void before(HistoryDetailEntity historyDetail) {
        }

        @Override
        public void after(HistoryDetailEntity historyDetail, ExecResult execResult) {
        }
    }
}
