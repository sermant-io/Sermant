package com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.v8;

import static org.mockito.Mockito.verify;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * mysql连接拦截器单元测试
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-10
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionCreateInterceptorTest {
    private ConnectionCreateInterceptor interceptor;

    @Mock
    private EnhancedInstance objectInstance;

    @Before
    public void setUp() {
        interceptor = new ConnectionCreateInterceptor();
    }

    @Test
    public void testResultIsEnhanceInstance() throws Throwable {
        interceptor.afterMethod(null, null, new Object[]{
            "localhost",
            3360,
            null,
            "test",
            "jdbc:mysql:replication://localhost:3360,localhost:3360,localhost:3360/test?useUnicode=true&characterEncoding=utf8&useSSL=false&roundRobinLoadBalance=true"
        }, null, objectInstance);
        verify(objectInstance).setSkyWalkingDynamicField(Matchers.any());
    }
}