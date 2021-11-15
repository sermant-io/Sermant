package com.huawei.recordconsole.desensitization;

import com.alibaba.fastjson.JSON;
import com.huawei.recordconsole.config.CommonConfig;
import com.huawei.recordconsole.entity.Recorder;
import com.huawei.recordconsole.zookeeper.ZookeeperUtil;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ZookeeperUtil.class)
public class DataDesensitizeTest {
    private Recorder recorder;

    private String mockReturn;

    private DataDesensitizeImpl dataDesensitizeimpl = new DataDesensitizeImpl();

    private CuratorFramework zkClient;

    @Before
    public void setUp() throws Exception {
        recorder = new Recorder();
        recorder.setAppType("Dubbo");
        recorder.setEntry(false);
        recorder.setJobId("fb2f7fe7-3d38-49ba-9117-6ab28f4cd7a7");
        recorder.setRequestClass("interface com.alibaba.dubbo.rpc.Invocation");
        recorder.setResponseClass("class java.lang.String");
        recorder.setTraceId("13716820ef22401eaa1ed056e32559a8.402.16170055438200001");
        recorder.setRequestBody("{\"arguments\":[\"360123199012301917\"],\"attachments\":{\"input\":\"334\",\"path\":\"com.example.common.service.BillOperation\",\"sw8-x\":\"0\",\"X-B3-SpanId\":\"2c1f4215c45fd690\",\"sw8\":\"1-MTM3MTY4MjBlZjIyNDAxZWFhMWVkMDU2ZTMyNTU5YTguNDAyLjE2MTcwMDU1NDM4MjAwMDAx-MTM3MTY4MjBlZjIyNDAxZWFhMWVkMDU2ZTMyNTU5YTguNDAyLjE2MTcwMDU1NDM4MjAwMDAw-1-WW91cl9BcHBsaWNhdGlvbk5hbWU=-M2VjNGFjN2RlY2I0NDg2ODgyYTZiNWE3YmIxMjQwOWFAMTAuMC4wLjU3-Y29tLmV4YW1wbGUuY29tbW9uLnNlcnZpY2UuT3JkZXJPcGVyYXRpb24uY3JlYXRlT3JkZXIoKQ==-MTE2LjYzLjE4MC4yMjA6MjA4ODE=-ZmFsc2U=\",\"X-B3-ParentSpanId\":\"3c18bab3224178b9\",\"X-B3-Sampled\":\"1\",\"X-B3-TraceId\":\"00e10a679a0aa157\",\"sw8-correlation\":\"\",\"interface\":\"com.example.common.service.BillOperation\",\"version\":\"0.0.0\"},\"methodName\":\"load\",\"parameterTypes\":[\"String\"]}");
        recorder.setMethodName("load");
        recorder.setResponseBody("\"360123199012301917\"");

        PowerMockito.mockStatic(ZookeeperUtil.class);
    }

    @Test
    public void coverDesensitize() throws Exception {
        String path = CommonConfig.PROJECT_NODE + CommonConfig.DESENSITIZE_NODE
                + CommonConfig.SLASH + "com.example.common.service.BillOperation" + CommonConfig.SLASH + recorder.getMethodName();
        mockReturn = "{\"[1-9]\\\\d{5}(18|19|([23]\\\\d))\\\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\\\d{3}[0-9Xx]\":{\"regex\":\"(?<=\\\\d{14})\\\\d\",\"symbol\":\"*\"}}";
        PowerMockito.when(ZookeeperUtil.getData(path, zkClient)).thenReturn(mockReturn);
        Assert.assertEquals(dataDesensitizeimpl.coverDesensitize(recorder, JSON.parseObject(recorder.getRequestBody()),
                "com.example.common.service.BillOperation").getResponseBody(), "\"36012319901230****\"");

    }

    @Test
    public void offsetDesensitize() throws Exception {
        String path = CommonConfig.PROJECT_NODE + CommonConfig.DESENSITIZE_NODE
                + CommonConfig.SLASH + "com.example.common.service.BillOperation" + CommonConfig.SLASH + recorder.getMethodName();
        mockReturn = "{\"[1-9]\\\\d{5}(18|19|([23]\\\\d))\\\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\\\d{3}[0-9Xx]\":5}";
        PowerMockito.when(ZookeeperUtil.getData(path, zkClient)).thenReturn(mockReturn);
        Assert.assertEquals(dataDesensitizeimpl.offsetDesensitize(recorder, JSON.parseObject(recorder.getRequestBody()),
                "com.example.common.service.BillOperation").getResponseBody(), "\"815678644567856462\"");

    }
}