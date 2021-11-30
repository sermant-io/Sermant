/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.mockserver.strategy;

import com.huawei.flowre.mockserver.config.MSConst;
import com.huawei.flowre.mockserver.datasource.EsDataSource;
import com.huawei.flowre.mockserver.domain.MockRequest;
import com.huawei.flowre.mockserver.domain.SelectResult;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 默认Mock查找策略，通过key精确匹配Mock结果进行返回
 *
 * @author luanwenfei
 * @version 1.0
 * @since 2021-02-03
 */
@Component
public class DefaultMockStrategy extends AbstractMockStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMockStrategy.class);

    @Autowired
    EsDataSource esDataSource;

    @Override
    public SelectResult selectMockResult(MockRequest mockRequest) {
        try {
            SelectResult selectResult = new SelectResult();
            List<String> subCallDataList = esDataSource
                    .searchByKey(MSConst.SUB_CALL_RECORD_PREFIX + mockRequest.getRecordJobId(),
                            MSConst.SUB_CALL_KEY, mockRequest.getSubCallKey());

            for (String str : subCallDataList) {
                if (!StringUtils.isEmpty(str)) {
                    JSONObject recordObject = JSON.parseObject(str);
                    if (recordObject.getString(MSConst.APP_TYPE).equals(MSConst.DUBBO)) {
                        JSONObject invocation = JSON.parseObject(recordObject.getString(MSConst.REQUEST_BODY));
                        if (JSONArray.parseArray(mockRequest.getArguments())
                                .equals(JSONArray.parseArray(invocation.getString("arguments")))) {
                            selectResult.setSelectClassName(recordObject.getString(MSConst.RESPONSE_CLASS));
                            selectResult.setSelectContent(recordObject.getString(MSConst.RESPONSE_BODY));
                            break;
                        }
                    } else if (recordObject.getString(MSConst.APP_TYPE).equals(MSConst.REDISSON)) {
                        if (mockRequest.getSubCallCount()
                                .equals(recordObject.getString(MSConst.SUB_CALL_COUNT))) {
                            selectResult.setSelectClassName(recordObject.getString(MSConst.RESPONSE_CLASS));
                            selectResult.setSelectContent(recordObject.getString(MSConst.RESPONSE_BODY));
                            break;
                        }
                    } else if (recordObject.getString(MSConst.REQUEST_BODY)
                            .equals(mockRequest.getArguments())) {
                        selectResult.setSelectClassName(recordObject.getString(MSConst.RESPONSE_CLASS));
                        selectResult.setSelectContent(recordObject.getString(MSConst.RESPONSE_BODY));
                        break;
                    }
                }
            }
            return selectResult;
        } catch (Exception exception) {
            LOGGER.error("Get sub call mock result error , {}", exception.getMessage());
        }
        return new SelectResult();
    }
}
