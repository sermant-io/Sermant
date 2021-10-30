/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.executor;

import com.huawei.common.ws.WebSocketServer;
import com.huawei.emergency.entity.HistoryDetailEntity;
import com.huawei.emergency.mapper.HistoryMapper;
import com.huawei.script.exec.ExecResult;
import com.huawei.script.exec.log.LogMemoryStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 脚本执行过程中的回调处理<br>
 * <P>主要用于处理脚本执行前后发生的一些状态转换</P>
 *
 * @author y30010171
 * @since 2021-10-27
 **/
@Component
@Transactional
public class DefaultScriptCallBack implements ScriptCallBack {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultScriptCallBack.class);
    private static final int RUNNING = 1;
    private static final int SUCCESS = 2;
    private static final int ERROR = 4;

    @Autowired
    private HistoryMapper historyMapper;

    @Override
    public void before(HistoryDetailEntity historyDetail) {
        HistoryDetailEntity needUpdateHistoryDetail = new HistoryDetailEntity();
        needUpdateHistoryDetail.setId(historyDetail.getId());
        needUpdateHistoryDetail.setStatus(RUNNING);
        historyMapper.updateHistoryDetailsStatusAndLog(needUpdateHistoryDetail);
        LOGGER.info("Task {} is running now.", historyDetail.getId());

        // 推送前端刷新
        pushRefresh(historyDetail.getHistoryId(),historyDetail.getSceneId());
    }

    @Override
    public void after(HistoryDetailEntity historyDetail, ExecResult execResult) {
        HistoryDetailEntity needUpdateHistoryDetail = new HistoryDetailEntity();
        needUpdateHistoryDetail.setId(historyDetail.getId());
        needUpdateHistoryDetail.setStatus(execResult.isSuccess() ? SUCCESS : ERROR);
        needUpdateHistoryDetail.setLog(execResult.getMsg());
        historyMapper.updateHistoryDetailsStatusAndLog(needUpdateHistoryDetail);
        LOGGER.info("Task {} is shutdown now.", historyDetail.getId());

        // 推送前端刷新
        pushRefresh(historyDetail.getHistoryId(),historyDetail.getSceneId());

        // 清除实时日志的在内存中的日志残留
        LogMemoryStore.removeLog(historyDetail.getId());
    }

    private void pushRefresh(int historyId, int sceneId) {
        WebSocketServer.sendMessage("/history/" + historyId);
        WebSocketServer.sendMessage("/scenario/" + sceneId);
    }
}
