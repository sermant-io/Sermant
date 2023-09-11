package com.huaweicloud.sermant.tag.transmission.interceptors.rpc.servicecomb;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficTag;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.interceptors.rpc.AbstractRpcInterceptorTest;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * ServiceCombRpcProviderInterceptor类的单元测试
 *
 * @author daizhenyu
 * @since 2023-08-30
 **/
public class ServiceCombRpcProviderInterceptorTest extends AbstractRpcInterceptorTest {
    private final ServiceCombRpcProviderInterceptor interceptor = new ServiceCombRpcProviderInterceptor();

    public ServiceCombRpcProviderInterceptorTest() {
    }

    @Override
    public void doBefore(TrafficTag trafficTag) {
    }

    @Test
    public void testServiceCombRpcProvider() {
        // 定义参数
        Invocation invocation;
        Object[] arguments;
        Object obj = new Object();
        ExecuteContext context;
        ExecuteContext returnContext;
        Map<String, List<String>> expectTrafficTag;
        HttpServletRequestEx requestEx;

        // Invocation 为null
        context = buildContext(null);
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag());

        // 流量标签透传开关关闭
        tagTransmissionConfig.setEnabled(false);
        context = buildContext(new Invocation());
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag());
        tagTransmissionConfig.setEnabled(true);

        // Invocation包含完整的tag
        invocation = new Invocation();
        invocation.getContext().put("id", "001");
        invocation.getContext().put("name", "test001");
        arguments = new Object[]{invocation};
        context = ExecuteContext.forMemberMethod(obj, null, arguments, null, null);
        returnContext = interceptor.before(context);
        expectTrafficTag = buildExpectTrafficTag("id", "name");
        Assert.assertEquals(TrafficUtils.getTrafficTag().getTag(), expectTrafficTag);
        interceptor.after(returnContext);

        // Invocation包含部分的tag
        invocation = new Invocation();
        invocation.getContext().put("id", "001");
        arguments = new Object[]{invocation};
        context = ExecuteContext.forMemberMethod(obj, null, arguments, null, null);
        returnContext = interceptor.before(context);
        expectTrafficTag = buildExpectTrafficTag("id");
        Assert.assertEquals(TrafficUtils.getTrafficTag().getTag(), expectTrafficTag);
        interceptor.after(returnContext);

        // 使用非servicecombrpc方式调用provider
        invocation = new Invocation();
        requestEx = Mockito.mock(HttpServletRequestEx.class);
        Vector<String> keyVector = new Vector<>();
        keyVector.add("id");
        keyVector.add("name");
        Enumeration<String> headerNames = keyVector.elements();
        Mockito.when(requestEx.getHeaderNames()).thenReturn(headerNames);
        Vector<String> idVector = new Vector<>();
        idVector.add("001");
        Enumeration<String> ids = idVector.elements();
        Vector<String> nameVector = new Vector<>();
        nameVector.add("test001");
        Enumeration<String> names = nameVector.elements();
        Mockito.when(requestEx.getHeaders("name")).thenReturn(names);
        Mockito.when(requestEx.getHeaders("id")).thenReturn(ids);
        invocation.onStart(requestEx, 1000L);
        arguments = new Object[]{invocation};
        context = ExecuteContext.forMemberMethod(obj, null, arguments, null, null);
        returnContext = interceptor.before(context);
        expectTrafficTag = buildExpectTrafficTag("id", "name");
        Assert.assertEquals(TrafficUtils.getTrafficTag().getTag(), expectTrafficTag);
        interceptor.after(returnContext);
    }

    @After
    public void afterTest() {
        Mockito.clearAllCaches();
    }

    private ExecuteContext buildContext(Invocation invocation) {
        Object[] arguments = new Object[]{invocation};
        return ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }
}