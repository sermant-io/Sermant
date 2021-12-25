package com.huawei.flowrecordreplay.console.service;

import com.alibaba.fastjson.JSON;
import com.huawei.flowrecordreplay.console.datasource.elasticsearch.EsDataSource;
import com.huawei.flowrecordreplay.console.datasource.entity.Ngrinder.MethodContent;
import com.huawei.flowrecordreplay.console.datasource.entity.Ngrinder.RecordContent;
import com.huawei.flowrecordreplay.console.datasource.entity.recordresult.RecordEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NgrinderServiceTest {
    @Autowired
    NgrinderService ngrinderService;

    @MockBean
    EsDataSource esDataSource;

    @Test
    public void generateNgrinder() throws Exception {
        String jobId = "jobId";
        RecordContent recordContent = new RecordContent();
        Map<String, List<MethodContent>> recordContent1 = new HashMap<>();
        Map<String, List<String>> keyList = new HashMap<>();
        Map<String, Integer> paramsCount = new HashMap<>();
        Map<String, MethodContent> originModel = new HashMap<>();
        recordContent.setRecordContent(recordContent1);
        recordContent.setParamsCount(paramsCount);
        recordContent.setOriginModel(originModel);
        recordContent.setKeyList(keyList);


        String str1 = "{\n" +
                "            \"jobId\": \"jobid\",\n" +
                "            \"traceId\": \"traceId\",\n" +
                "            \"appType\": \"HTTP\",\n" +
                "            \"methodName\": \"POST /pm/v1/{project_id}/metric-data\",\n" +
                "            \"subCallKey\": \"subCallKey\",\n" +
                "            \"subCallCount\": \"0\",\n" +
                "            \"requestBody\": \"{\\\"headMap\\\":{\\\"scheme\\\":\\\"https\\\",\\\"Content-Type\\\":\\\"application/json\\\",\\\"User-Agent\\\":\\\"python\\\"},\\\"httpRequestBody\\\":\\\"{\\\\\\\"collectTime\\\\\\\":\\\\\\\"162121231231\\\\\\\",\\\\\\\"metric\\\\\\\":\\\\\\\"name\\\\\\\"}\\\",\\\"method\\\":\\\"POST\\\",\\\"url\\\":\\\"/V1.0/TENANT/metric-data\\\"}\",\n" +
                "            \"requestClass\": \"java.lang.String\",\n" +
                "            \"responseBody\": \"responseBody\",\n" +
                "            \"responseClass\": \"java.lang.String\",\n" +
                "            \"timestamp\": \"2021-04-13 15:16:52\"\n" +
                "}";

        String str2 = "{\n" +
                "            \"jobId\": \"jobid\",\n" +
                "            \"traceId\": \"traceId\",\n" +
                "            \"appType\": \"HTTP\",\n" +
                "            \"methodName\": \"POST /pm/v1/{project_id}/metric-data\",\n" +
                "            \"subCallKey\": \"subCallKey\",\n" +
                "            \"subCallCount\": \"0\",\n" +
                "            \"requestBody\": \"{\\\"headMap\\\":{\\\"scheme\\\":\\\"https1\\\",\\\"Content-Type\\\":\\\"application/json\\\",\\\"User-Agent\\\":\\\"python\\\"},\\\"httpRequestBody\\\":\\\"{\\\\\\\"collectTime\\\\\\\":\\\\\\\"162121231231\\\\\\\",\\\\\\\"metric\\\\\\\":\\\\\\\"name\\\\\\\"}\\\",\\\"method\\\":\\\"POST\\\",\\\"url\\\":\\\"/V1.0/TENANT/metric-data\\\"}\",\n" +
                "            \"requestClass\": \"java.lang.String\",\n" +
                "            \"responseBody\": \"responseBody\",\n" +
                "            \"responseClass\": \"java.lang.String\",\n" +
                "            \"timestamp\": \"2021-04-13 15:16:52\"\n" +
                "}";

        String str3 = "{\n" +
                "            \"jobId\": \"jobid\",\n" +
                "            \"traceId\": \"traceId\",\n" +
                "            \"appType\": \"HTTP\",\n" +
                "            \"methodName\": \"POST /pm/v1/{project_id}/metric-data\",\n" +
                "            \"subCallKey\": \"subCallKey\",\n" +
                "            \"subCallCount\": \"0\",\n" +
                "            \"requestBody\": \"{\\\"headMap\\\":{\\\"scheme\\\":\\\"https1\\\",\\\"Content-Type\\\":\\\"application/json\\\",\\\"User-Agent\\\":\\\"python\\\"},\\\"httpRequestBody\\\":\\\"{\\\\\\\"collectTime\\\\\\\":\\\\\\\"162121231231\\\\\\\",\\\\\\\"metric\\\\\\\":\\\\\\\"name\\\\\\\"}\\\",\\\"method\\\":\\\"POST\\\",\\\"url\\\":\\\"/V1.0/TENANT/metric-data\\\"}\",\n" +
                "            \"requestClass\": \"java.lang.String\",\n" +
                "            \"responseBody\": \"responseBody\",\n" +
                "            \"responseClass\": \"java.lang.String\",\n" +
                "            \"timestamp\": \"2021-04-13 15:16:52\"\n" +
                "}";

        String str4 = "{\n" +
                "            \"jobId\": \"jobid\",\n" +
                "            \"traceId\": \"traceId\",\n" +
                "            \"appType\": \"HTTP\",\n" +
                "            \"methodName\": \"POST /pm/v1/{project_id}/metric-data\",\n" +
                "            \"subCallKey\": \"subCallKey\",\n" +
                "            \"subCallCount\": \"0\",\n" +
                "            \"requestBody\": \"{\\\"headMap\\\":{\\\"scheme\\\":\\\"https\\\",\\\"Content-Type\\\":\\\"application/json\\\",\\\"User-Agent\\\":\\\"python\\\"},\\\"httpRequestBody\\\":\\\"{\\\\\\\"collectTime\\\\\\\":\\\\\\\"162121231231\\\\\\\",\\\\\\\"metric\\\\\\\":\\\\\\\"name\\\\\\\"}\\\",\\\"method\\\":\\\"POST\\\",\\\"url\\\":\\\"/V1.0/TENANT/metric-data\\\"}\",\n" +
                "            \"requestClass\": \"java.lang.String\",\n" +
                "            \"responseBody\": \"responseBody\",\n" +
                "            \"responseClass\": \"java.lang.String\",\n" +
                "            \"timestamp\": \"2021-04-13 15:16:52\"\n" +
                "}";

        String str5 = "{\n" +
                "            \"jobId\": \"jobid\",\n" +
                "            \"traceId\": \"traceId\",\n" +
                "            \"appType\": \"HTTP\",\n" +
                "            \"methodName\": \"POST /pm/v1/{project_id}/metric-data\",\n" +
                "            \"subCallKey\": \"subCallKey\",\n" +
                "            \"subCallCount\": \"0\",\n" +
                "            \"requestBody\": \"{\\\"headMap\\\":{\\\"scheme\\\":\\\"https\\\",\\\"Content-Type\\\":\\\"application/json\\\",\\\"User-Agent\\\":\\\"python\\\"},\\\"httpRequestBody\\\":\\\"{\\\\\\\"collectTime\\\\\\\":\\\\\\\"162121231231@\\\\\\\",\\\\\\\"metric\\\\\\\":\\\\\\\"name\\\\\\\"}\\\",\\\"method\\\":\\\"POST\\\",\\\"url\\\":\\\"/V1.0/TENANT/metric-data\\\"}\",\n" +
                "            \"requestClass\": \"java.lang.String\",\n" +
                "            \"responseBody\": \"responseBody\",\n" +
                "            \"responseClass\": \"java.lang.String\",\n" +
                "            \"timestamp\": \"2021-04-13 15:16:52\"\n" +
                "}";

        List<String> recordResultString = new ArrayList<>();
        recordResultString.add(str1);
        recordResultString.add(str2);
        recordResultString.add(str3);
        recordResultString.add(str4);
        recordResultString.add(str5);

        Mockito.when(esDataSource.getAllData(jobId + "*")).thenReturn(recordResultString);
        for (String str : recordResultString) {
            RecordEntity recordEntity = JSON.parseObject(str, RecordEntity.class);
            ngrinderService.putIn(recordContent, recordEntity);
        }
        assert(recordContent.getKeyList().containsKey("POST /pm/v1/{project_id}/metric-data"));
    }

}