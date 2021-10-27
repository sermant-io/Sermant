package com.huawei.flowre.flowreplay.service;

import com.huawei.flowre.flowreplay.domain.ReplayResultEntity;

import com.alibaba.fastjson.JSONObject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-26
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ResultCompareServiceTest {
    @Autowired
    ResultCompareService resultCompareService;

    @Test
    public void compare() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class clazz = resultCompareService.getClass();
        Method compareObject = clazz.getDeclaredMethod("compareResult", JSONObject.class, JSONObject.class);
        Method compareString = clazz.getDeclaredMethod("compareResult", String.class, String.class);
        compareObject.setAccessible(true);
        compareString.setAccessible(true);

        /**
         * 测试相同的情况
         */
        JSONObject jsonObjectA = new JSONObject();
        jsonObjectA.put("FieldA", "true");
        jsonObjectA.put("FieldB", "false");
        JSONObject jsonObjectB = new JSONObject();
        jsonObjectB.put("FieldA", "true");
        jsonObjectB.put("FieldB", "false");
        ReplayResultEntity replayResultEntity = (ReplayResultEntity) compareObject.invoke(resultCompareService, jsonObjectA, jsonObjectB);
        Assert.assertTrue(replayResultEntity.isCompareResult());

        /**
         * 测试不相同的情况
         */
        jsonObjectA.put("FieldB", "true");
        replayResultEntity = (ReplayResultEntity) compareObject.invoke(resultCompareService, jsonObjectA, jsonObjectB);
        Assert.assertFalse(replayResultEntity.isCompareResult());

        /**
         * 测试字符串比对相同情况
         */
        String strA = "true";
        String strB = "true";
        replayResultEntity = (ReplayResultEntity) compareString.invoke(resultCompareService, strA, strB);
        Assert.assertTrue(replayResultEntity.isCompareResult());

        /**
         * 测试字符串比对不同情况
         */
        strB = "false";
        replayResultEntity = (ReplayResultEntity) compareString.invoke(resultCompareService, strA, strB);
        Assert.assertFalse(replayResultEntity.isCompareResult());
    }
}