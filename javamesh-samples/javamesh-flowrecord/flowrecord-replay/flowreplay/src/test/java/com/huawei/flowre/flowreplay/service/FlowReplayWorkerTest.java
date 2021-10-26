package com.huawei.flowre.flowreplay.service;

import com.huawei.flowre.flowreplay.config.Const;
import com.huawei.flowre.flowreplay.utils.ZookeeperUtil;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.GetChildrenBuilderImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(ZookeeperUtil.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FlowReplayWorkerTest {
    @Autowired
    FlowReplayWorker flowReplayWorker;

    @MockBean
    CuratorFramework zkClient;

    @Mock
    GetChildrenBuilderImpl getChildrenBuilder;

    @Test
    public void testIsTurnToMe() throws Exception {
        Class clazz = flowReplayWorker.getClass();
        List<String> list = new ArrayList<>();
        list.add("worker_name_01");
        list.add("worker_name_02");
        list.add("worker_name_03");
        list.add("worker_name_04");
        list.add("worker_name_05");
        Mockito.when(zkClient.getChildren()).thenReturn(getChildrenBuilder);
        Mockito.when(getChildrenBuilder.forPath(Const.REPLAY_LOCK_PATH)).thenReturn(list);
        PowerMockito.mockStatic(ZookeeperUtil.class);
        PowerMockito.when(ZookeeperUtil.getData(Mockito.any(), Mockito.any())).thenReturn("\"worker_name_01\"");
        Method isTurnToMe = clazz.getDeclaredMethod("isTurnToMe", String.class);
        isTurnToMe.setAccessible(true);
        Assert.assertTrue((boolean) isTurnToMe.invoke(flowReplayWorker, "worker_name_01"));
    }
}