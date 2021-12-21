/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.log;

import com.huawei.emergency.entity.EmergencyExecRecordDetail;
import com.huawei.emergency.entity.EmergencyExecRecordDetailExample;
import com.huawei.emergency.mapper.EmergencyExecRecordDetailMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * 记录每次脚本执行的实时日志
 *
 * @author y30010171
 * @since 2021-10-26
 **/
@Component
public class DefaultLogCallBack implements LogCallBack {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLogCallBack.class);

    @Autowired
    EmergencyExecRecordDetailMapper detailMapper;

    @Override
    public void handleLog(int id, String log) {
        LogMemoryStore.addLog(id, new String[]{log});
    }

    @Override
    public void handlePid(int id, String pid) {
        try {
            EmergencyExecRecordDetail recordDetail = new EmergencyExecRecordDetail();
            recordDetail.setDetailId(id);
            recordDetail.setPid(Integer.valueOf(pid));
            detailMapper.updateByPrimaryKeySelective(recordDetail);
        } catch (NumberFormatException e) {
            LOGGER.error("cast log to pid error. {}", e.getMessage());
            detailMapper.updateLogIfAbsent(id,pid);
        }
    }
}
