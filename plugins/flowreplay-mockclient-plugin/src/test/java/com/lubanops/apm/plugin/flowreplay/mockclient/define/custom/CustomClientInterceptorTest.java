package com.lubanops.apm.plugin.flowreplay.mockclient.define.custom;

import com.lubanops.apm.plugin.flowreplay.mockclient.config.PluginConfig;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.MockAction;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.MockResult;
import com.lubanops.apm.plugin.flowreplay.mockclient.domain.SelectResult;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 自定义拦截插件测试
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-05-11
 */
public class CustomClientInterceptorTest {
    @Test
    public void checkMockResult() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        // 空的mock result
        MockResult mockResultA = null;

        // 不支持类型的mock result
        MockResult mockResultB = new MockResult();
        mockResultB.setMockRequestType(PluginConfig.NOTYPE);

        // 跳过的mock result
        MockResult mockResultC = new MockResult();
        mockResultC.setMockRequestType(PluginConfig.CUSTOM_TYPE);
        mockResultC.setMockAction(MockAction.SKIP);

        // 结果为空的mock result
        MockResult mockResultD = new MockResult();
        mockResultD.setMockRequestType(PluginConfig.CUSTOM_TYPE);
        mockResultD.setMockAction(MockAction.RETURN);
        mockResultD.setSelectResult(new SelectResult());

        // 结果符合规则的mock result
        MockResult mockResultE = new MockResult();
        mockResultE.setMockRequestType(PluginConfig.CUSTOM_TYPE);
        mockResultE.setMockAction(MockAction.RETURN);
        SelectResult selectResult = new SelectResult();
        selectResult.setSelectContent("content");
        selectResult.setSelectClassName("class");
        mockResultE.setSelectResult(selectResult);

        Method method = CustomClientInterceptor.class.getDeclaredMethod("checkMockResult", MockResult.class, String.class);
        method.setAccessible(true);
        Assert.assertFalse((boolean) method.invoke(CustomClientInterceptor.class.newInstance(), mockResultA, "subCallKey"));
        Assert.assertFalse((boolean) method.invoke(CustomClientInterceptor.class.newInstance(), mockResultB, "subCallKey"));
        Assert.assertFalse((boolean) method.invoke(CustomClientInterceptor.class.newInstance(), mockResultC, "subCallKey"));
        Assert.assertFalse((boolean) method.invoke(CustomClientInterceptor.class.newInstance(), mockResultD, "subCallKey"));
        Assert.assertTrue((boolean) method.invoke(CustomClientInterceptor.class.newInstance(), mockResultE, "subCallKey"));
    }
}