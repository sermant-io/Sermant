package com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.common;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.plugin.jdbc.trace.ConnectionInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

/**
 * prepared statement 拦截器单元测试
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-10
 */

/**
 * prepared statement 拦截器单元测试
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-09
 */
public class PreparedStatementInterceptorTest {
    private PreparedStatementInterceptor interceptor;
    @Mock
    private EnhancedInstance ret;

    @Mock
    private EnhancedInstance objectInstance;

    @Mock
    private ConnectionInfo connectionInfo;

    @Before
    public void setUp() {
        interceptor = new PreparedStatementInterceptor();
        when(objectInstance.getSkyWalkingDynamicField()).thenReturn(connectionInfo);
    }

    @Test
    public void testResultIsEnhanceInstance() throws Throwable {
        interceptor.afterMethod(objectInstance, null, new Object[]{"SELECT * FROM test"}, null, ret);
        verify(ret).setSkyWalkingDynamicField(Matchers.any());
    }
}