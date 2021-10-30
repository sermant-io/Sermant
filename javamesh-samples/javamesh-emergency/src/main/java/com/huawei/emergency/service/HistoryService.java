/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service;

import com.huawei.common.api.CommonResult;
import com.huawei.emergency.dto.ListDetailsDto;
import com.huawei.emergency.dto.ListHistoryParam;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * 任务管理接口
 *
 * @since 2021-10-30
 */
public interface HistoryService {
    CommonResult listHistory(ListHistoryParam param);

    List<ListDetailsDto> listDetails(int historyId);

    void downloadLog(int id, HttpServletResponse response);
}
