package com.huawei.flowre.mockserver.service;


import static org.junit.Assert.assertEquals;

import com.huawei.flowre.mockserver.domain.MockRequestType;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MockResponseServiceTest {
    @Autowired
    MockResponseService mockResponseService;

    @MockBean
    CuratorFramework zkClient;

    @Test
    public void isMockRequestType() {
        assertEquals(MockRequestType.DUBBO, mockResponseService.isMockRequestType("dubbo"));
        assertEquals(MockRequestType.HTTP, mockResponseService.isMockRequestType("http"));
        assertEquals(MockRequestType.MYSQL, mockResponseService.isMockRequestType("mysql"));
        assertEquals(MockRequestType.CUSTOM, mockResponseService.isMockRequestType("custom"));
        assertEquals(MockRequestType.REDIS, mockResponseService.isMockRequestType("redis"));
        assertEquals(MockRequestType.NOTYPE, mockResponseService.isMockRequestType("abc"));
    }

    @Test
    public void isSkipMethod() {
        Assert.assertTrue(mockResponseService.isSkipMethod("isSkip"));
    }
}