package com.huawei.flowre.mockserver.strategy;

import com.huawei.flowre.mockserver.config.MSConst;
import com.huawei.flowre.mockserver.datasource.EsDataSource;
import com.huawei.flowre.mockserver.domain.MockRequest;
import com.huawei.flowre.mockserver.domain.MockRequestType;

import com.alibaba.fastjson.JSON;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-05-11
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class DefaultMockStrategyTest {
    @Autowired
    DefaultMockStrategy defaultMockStrategy;

    @MockBean
    EsDataSource esDataSource;

    @Test
    public void selectMockResult() throws IOException {
        String redisson = "{\"appType\":\"Redisson\",\"jobId\":\"jobId\",\"methodName\":\"method\"," +
                "\"requestBody\":\"[\\\"demo\\\",1]\",\"requestClass\":\"org.redisson.command.CommandAsyncService\"," +
                "\"responseBody\":\"1\",\"responseClass\":\"java.lang.Long\",\"subCallCount\":2," +
                "\"subCallKey\":\"subCallKey\",\"timestamp\":null,\"traceId\":\"traceId\"}";
        String custom = "{\"appType\":\"Custom\",\"jobId\":\"jobId\",\"methodName\":\"method\"," +
                "\"requestBody\":\"[\\\"arguments\\\"]\",\"requestClass\":\"com.huawei.test\",\"responseBody\":\"1\"," +
                "\"responseClass\":\"java.lang.Long\",\"subCallCount\":2,\"subCallKey\":\"subCallKey\"," +
                "\"timestamp\":null,\"traceId\":\"traceId\"}";
        String dubbo = "{\"appType\":\"Dubbo\",\"jobId\":\"jobId\",\"methodName\":\"method\"," +
                "\"requestBody\":\"{\\\"arguments\\\":[\\\"arguments\\\"]}\"," +
                "\"requestClass\":\"com.alibaba.dubbo.rpc.Invocation\",\"responseBody\":\"1\"," +
                "\"responseClass\":\"java.lang.Long\",\"subCallCount\":2,\"subCallKey\":\"subCallKey\"," +
                "\"timestamp\":null,\"traceId\":\"traceId\"}";
        List<String> dubboDataList = new ArrayList<>();
        List<String> redissonDataList = new ArrayList<>();
        List<String> customDataList = new ArrayList<>();
        redissonDataList.add(redisson);
        dubboDataList.add(dubbo);
        customDataList.add(custom);
        Mockito.when(esDataSource.searchByKey(MSConst.SUB_CALL_RECORD_PREFIX + "recordJobId",
                MSConst.SUB_CALL_KEY, "subCallKeyRedis")).thenReturn(redissonDataList);
        Mockito.when(esDataSource.searchByKey(MSConst.SUB_CALL_RECORD_PREFIX + "recordJobId",
                MSConst.SUB_CALL_KEY, "subCallKeyDubbo")).thenReturn(dubboDataList);
        Mockito.when(esDataSource.searchByKey(MSConst.SUB_CALL_RECORD_PREFIX + "recordJobId",
                MSConst.SUB_CALL_KEY, "subCallKeyCustom")).thenReturn(customDataList);
        MockRequest mockResultRedisson = new MockRequest();
        mockResultRedisson.setSubCallKey("subCallKeyRedis");
        mockResultRedisson.setSubCallCount("2");
        mockResultRedisson.setMockRequestType(MockRequestType.REDIS.getName());
        mockResultRedisson.setMethod("methodName");
        mockResultRedisson.setRecordJobId("recordJobId");
        mockResultRedisson.setArguments("[\\\"arguments\\\"]");

        MockRequest mockResultDubbo = new MockRequest();
        mockResultDubbo.setSubCallKey("subCallKeyDubbo");
        mockResultDubbo.setSubCallCount("2");
        mockResultDubbo.setMockRequestType(MockRequestType.DUBBO.getName());
        mockResultDubbo.setMethod("methodName");
        mockResultDubbo.setRecordJobId("recordJobId");
        mockResultDubbo.setArguments("[\"arguments\"]");

        MockRequest mockResultCustom = new MockRequest();
        mockResultCustom.setSubCallKey("subCallKeyCustom");
        mockResultCustom.setSubCallCount("2");
        mockResultCustom.setMockRequestType(MockRequestType.CUSTOM.getName());
        mockResultCustom.setMethod("methodName");
        mockResultCustom.setRecordJobId("recordJobId");
        mockResultCustom.setArguments("[\"arguments\"]");

        Assert.assertEquals("java.lang.Long",defaultMockStrategy.selectMockResult(mockResultRedisson).getSelectClassName());
        Assert.assertEquals("1",defaultMockStrategy.selectMockResult(mockResultRedisson).getSelectContent());
        Assert.assertEquals("java.lang.Long",defaultMockStrategy.selectMockResult(mockResultDubbo).getSelectClassName());
        Assert.assertEquals("1",defaultMockStrategy.selectMockResult(mockResultDubbo).getSelectContent());
        Assert.assertEquals("java.lang.Long",defaultMockStrategy.selectMockResult(mockResultCustom).getSelectClassName());
        Assert.assertEquals("1",defaultMockStrategy.selectMockResult(mockResultCustom).getSelectContent());
    }
}