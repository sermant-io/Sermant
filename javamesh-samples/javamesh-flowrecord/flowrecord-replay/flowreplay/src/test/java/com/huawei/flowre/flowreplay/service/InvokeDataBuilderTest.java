package com.huawei.flowre.flowreplay.service;

import com.huawei.flowre.flowreplay.config.Const;
import com.huawei.flowre.flowreplay.domain.ModifyRuleEntity;
import com.huawei.flowre.flowreplay.domain.RecordEntity;

import com.alibaba.fastjson.JSON;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-26
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class InvokeDataBuilderTest {
    @Autowired
    InvokeDataBuilder invokeDataBuilder;

    @Test
    public void modifyArguments() {
        ModifyRuleEntity entity1 = new ModifyRuleEntity();
        entity1.setType(Const.CONCRETE_TYPE);
        entity1.setSearch("Search me!");
        entity1.setReplacement("Replace success!");
        ModifyRuleEntity entity2 = new ModifyRuleEntity();
        entity2.setType(Const.REGEX_TYPE);
        entity2.setSearch("[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]");
        entity2.setReplacement("123456789123456789");
        List<ModifyRuleEntity> list = new ArrayList<>();
        list.add(entity1);
        list.add(entity2);
        ModifyRuleEntity entity3 = new ModifyRuleEntity();
        entity3.setType(Const.DATE_TYPE);
        entity3.setSearch("metric.1#dimensions.value");
        entity3.setReplacement("123456789123456789");
        List<ModifyRuleEntity> list1 = new ArrayList<>();
        list1.add(entity3);

        RecordEntity recordEntity = new RecordEntity();
        recordEntity.setAppType("Dubbo");
        recordEntity.setEntry(false);
        recordEntity.setJobId("fb2f7fe7-3d38-49ba-9117-6ab28f4cd7a7");
        recordEntity.setRequestClass("interface com.alibaba.dubbo.rpc.Invocation");
        recordEntity.setResponseClass("class java.lang.String");
        recordEntity.setTraceId("13716820ef22401eaa1ed056e32559a8.402.16170055438200001");
        recordEntity.setRequestBody("{\"arguments\":[\"Search me!\",\"360123199012301917\"],\"attachments\":{\"input\":\"334\",\"path\":\"com.example.common.service.BillOperation\",\"sw8-x\":\"0\",\"X-B3-SpanId\":\"2c1f4215c45fd690\",\"sw8\":\"1-MTM3MTY4MjBlZjIyNDAxZWFhMWVkMDU2ZTMyNTU5YTguNDAyLjE2MTcwMDU1NDM4MjAwMDAx-MTM3MTY4MjBlZjIyNDAxZWFhMWVkMDU2ZTMyNTU5YTguNDAyLjE2MTcwMDU1NDM4MjAwMDAw-1-WW91cl9BcHBsaWNhdGlvbk5hbWU=-M2VjNGFjN2RlY2I0NDg2ODgyYTZiNWE3YmIxMjQwOWFAMTAuMC4wLjU3-Y29tLmV4YW1wbGUuY29tbW9uLnNlcnZpY2UuT3JkZXJPcGVyYXRpb24uY3JlYXRlT3JkZXIoKQ==-MTE2LjYzLjE4MC4yMjA6MjA4ODE=-ZmFsc2U=\",\"X-B3-ParentSpanId\":\"3c18bab3224178b9\",\"X-B3-Sampled\":\"1\",\"X-B3-TraceId\":\"00e10a679a0aa157\",\"sw8-correlation\":\"\",\"interface\":\"com.example.common.service.BillOperation\",\"version\":\"0.0.0\"},\"methodName\":\"load\",\"parameterTypes\":[\"String\"]}");
        recordEntity.setMethodName("load");
        recordEntity.setResponseBody("\"\"");

        RecordEntity recordEntity1 = new RecordEntity();
        recordEntity1.setAppType("HTTP");
        recordEntity1.setEntry(false);
        recordEntity1.setJobId("fb2f7fe7-3d38-49ba-9117-6ab28f4cd7a7");
        recordEntity1.setRequestClass("interface com.alibaba.dubbo.rpc.Invocation");
        recordEntity1.setResponseClass("class java.lang.String");
        recordEntity1.setTraceId("13716820ef22401eaa1ed056e32559a8.402.16170055438200001");
        recordEntity1.setRequestBody("{\"httpRequestBody\": [{\"metric\":{\"namespace\": \"aha\", \"dimensions\":[{\"name\": \"instance_id\", \"value\": \"test\"}], \"metric_name\": \"cpu\"}, \"value\": 12.0}]}");
        recordEntity1.setMethodName("load");
        recordEntity1.setResponseBody("\"\"");

        Class clazz = invokeDataBuilder.getClass();
        try {
            Method modifyArguments = clazz.getDeclaredMethod("modifyArguments", recordEntity.getClass(), List.class);
            modifyArguments.setAccessible(true);
            modifyArguments.invoke(invokeDataBuilder, recordEntity, list);
            Method modifyArguments1 = clazz.getDeclaredMethod("modifyArguments", recordEntity1.getClass(), List.class);
            modifyArguments1.setAccessible(true);
            modifyArguments1.invoke(invokeDataBuilder, recordEntity1, list1);
            Assert.assertEquals(JSON.parseObject(recordEntity.getRequestBody()).get("arguments").toString(), "[\"Replace success!\",\"123456789123456789\"]");
            Assert.assertEquals(false, JSON.parseObject(recordEntity1.getRequestBody()).get("httpRequestBody").toString().contains("test"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}