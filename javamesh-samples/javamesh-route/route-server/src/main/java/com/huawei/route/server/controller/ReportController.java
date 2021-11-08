/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.huawei.route.common.Result;
import com.huawei.route.server.entity.AbstractInstance;
import com.huawei.route.server.entity.AbstractService;
import com.huawei.route.server.entity.ServiceRegistrarMessage;
import com.huawei.route.server.rules.GrayRuleManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 路由上报接口
 *
 * @author zhouss
 * @since 2021-10-13
 */
@RequestMapping(value = "/register/v1/report")
@RestController
public class ReportController<S extends AbstractService<T>, T extends AbstractInstance> {
    private GrayRuleManager<S, T> grayRuleManager;

    @Autowired
    private void setGrayRuleManager(GrayRuleManager<S, T> grayRuleManager) {
        this.grayRuleManager = grayRuleManager;
    }

    /**
     * 路由上报数据
     *
     * @param json  json串，{@link ServiceRegistrarMessage}
     * @return 响应结果
     */
    @PostMapping("/reportRegistry")
    public Result<Object> reportRegistry(@RequestBody String json) {
        if (!StringUtils.isEmpty(json)) {
            final List<ServiceRegistrarMessage> serviceRegistrarMessages;
            try {
                serviceRegistrarMessages = JSONArray.parseArray(json, ServiceRegistrarMessage.class);
            } catch (JSONException e) {
                return Result.ofFail(HttpStatus.SC_BAD_REQUEST, "参数不合法!");
            }
            grayRuleManager.updateRegistrarMessage(serviceRegistrarMessages);
        }
        return Result.ofSuccessMsg("success");
    }
}
