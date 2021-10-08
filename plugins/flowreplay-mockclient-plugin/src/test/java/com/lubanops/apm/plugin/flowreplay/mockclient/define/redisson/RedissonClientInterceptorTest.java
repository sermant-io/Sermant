package com.lubanops.apm.plugin.flowreplay.mockclient.define.redisson;

import com.lubanops.apm.plugin.flowreplay.mockclient.config.PluginConfig;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.MockAction;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.MockResult;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.SelectResult;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * redisson 拦截插件测试
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-05-11
 */
public class RedissonClientInterceptorTest {
    @Test
    public void checkMockResult() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        // 空的mock result
        MockResult mockResultA = null;

        // 不支持类型的mock result
        MockResult mockResultB = new MockResult();
        mockResultB.setMockRequestType(PluginConfig.NOTYPE);

        // 跳过的mock result
        MockResult mockResultC = new MockResult();
        mockResultC.setMockRequestType(PluginConfig.REDIS);
        mockResultC.setMockAction(MockAction.SKIP);

        // 结果为空的mock result
        MockResult mockResultD = new MockResult();
        mockResultD.setMockRequestType(PluginConfig.REDIS);
        mockResultD.setMockAction(MockAction.RETURN);
        mockResultD.setSelectResult(new SelectResult());

        // 结果符合规则的mock result
        MockResult mockResultE = new MockResult();
        mockResultE.setMockRequestType(PluginConfig.REDIS);
        mockResultE.setMockAction(MockAction.RETURN);
        SelectResult selectResult = new SelectResult();
        selectResult.setSelectContent("content");
        selectResult.setSelectClassName("class");
        mockResultE.setSelectResult(selectResult);

        Method method = RedissonClientInterceptor.class.getDeclaredMethod("checkMockResult", MockResult.class, String.class);
        method.setAccessible(true);
        Assert.assertFalse((boolean) method.invoke(RedissonClientInterceptor.class.newInstance(), mockResultA, "subCallKey"));
        Assert.assertFalse((boolean) method.invoke(RedissonClientInterceptor.class.newInstance(), mockResultB, "subCallKey"));
        Assert.assertFalse((boolean) method.invoke(RedissonClientInterceptor.class.newInstance(), mockResultC, "subCallKey"));
        Assert.assertFalse((boolean) method.invoke(RedissonClientInterceptor.class.newInstance(), mockResultD, "subCallKey"));
        Assert.assertTrue((boolean) method.invoke(RedissonClientInterceptor.class.newInstance(), mockResultE, "subCallKey"));
    }
}