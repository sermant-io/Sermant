/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.mockserver.controller;

import com.huawei.flowre.mockserver.datasource.EsDataSource;
import com.huawei.flowre.mockserver.domain.MockRequest;
import com.huawei.flowre.mockserver.domain.MockResult;
import com.huawei.flowre.mockserver.service.MockResponseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * controller 用于接收client请求返回mock结果
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-02-07
 */
@Controller
@RequestMapping("/mockserver")
public class MockController {
    @Autowired
    MockResponseService mockResponseService;

    @Autowired
    EsDataSource esDataSource;

    /**
     * 返回mock结果
     *
     * @param mockRequest mock请求
     * @return 返回封装好的MockResponse结果
     */
    @PostMapping("/result")
    @ResponseBody
    public MockResult getResult(@RequestBody MockRequest mockRequest) {
        return mockResponseService.getResponse(mockRequest);
    }
}
