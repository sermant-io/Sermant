/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.huawei.common.api.CommonResult;
import com.huawei.common.exception.ApiException;
import com.huawei.common.util.PageUtil;
import com.huawei.emergency.dto.DownloadLogDto;
import com.huawei.emergency.dto.ListDetailsDto;
import com.huawei.emergency.dto.ListHistoryParam;
import com.huawei.emergency.entity.HistoryEntity;
import com.huawei.emergency.mapper.HistoryMapper;
import com.huawei.emergency.service.HistoryService;

import com.github.pagehelper.PageHelper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * 任务管理接口的实现类
 *
 * @since 2021-10-30
 **/
@Service
@Transactional
@Slf4j
public class HistoryServiceImpl implements HistoryService {
    private static final int BUF_SIZE = 1024;

    @Autowired
    private HistoryMapper mapper;

    @Override
    public CommonResult listHistory(ListHistoryParam param) {
        if (param.getOrder().equals("ascend")) {
            param.setOrder("ASC");
        } else {
            param.setOrder("DESC");
        }
        PageHelper.orderBy(param.getSorter() + System.lineSeparator() + param.getOrder());
        List<HistoryEntity> historyList = mapper.listHistory(param);
        return CommonResult.success(
            PageUtil.startPage(historyList, param.getCurrent(), param.getPageSize()), historyList.size());
    }

    @Override
    public List<ListDetailsDto> listDetails(int historyId) {
        List<ListDetailsDto> detailsList = mapper.listHistoryDetails(historyId);
        for (ListDetailsDto detail : detailsList) {
            detail.setScriptNameAndUser(detail.getScriptName() + "(" + detail.getScriptUser() + ")");
        }
        return detailsList;
    }

    @Override
    public void downloadLog(int id, HttpServletResponse response) {
        DownloadLogDto downloadLogDto = mapper.selectDetailById(id);
        String logContext = downloadLogDto.getLog();
        logContext = logContext == null ? "" : logContext;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("log", "UTF-8"));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            inputStream = new ByteArrayInputStream(logContext.getBytes(StandardCharsets.UTF_8));
            byte[] buf = new byte[BUF_SIZE];
            int length = 0;
            outputStream = response.getOutputStream();
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
        } catch (IOException e) {
            throw new ApiException("下载日志文件失败");
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("Close stream failed");
            }
        }
    }
}
