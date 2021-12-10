/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.recordconsole.desensitization;

import com.huawei.recordconsole.config.CommonConfig;
import com.huawei.recordconsole.entity.GroovyInfoEntity;
import com.huawei.recordconsole.entity.Recorder;
import com.huawei.recordconsole.entity.ReplaceRegexEntity;
import com.huawei.recordconsole.zookeeper.ZookeeperUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import groovy.lang.GroovyObject;
import groovy.util.GroovyScriptEngine;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 录制数据脱敏接口实现类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-04-13
 */
@Component
public class DataDesensitizeImpl implements DataDesensitize {
    @Autowired
    private CuratorFramework zkClient;

    @Override
    public Recorder dubboDesensitize(Recorder recorder) throws Exception {
        JSONObject requestBody = JSON.parseObject(recorder.getRequestBody());
        JSONObject attachments = JSON.parseObject(requestBody.getString(CommonConfig.ATTACHMENTS_FIELD));
        String application = attachments.getString(CommonConfig.INTERFACE_FIELD);
        if (zkClient.checkExists().forPath(CommonConfig.PROJECT_NODE + CommonConfig.DESENSITIZE_NODE
                + CommonConfig.SLASH + application) == null) {
            return recorder;
        }
        String type = JSON.parseObject(ZookeeperUtil.getData(CommonConfig.PROJECT_NODE + CommonConfig.DESENSITIZE_NODE
                + CommonConfig.SLASH + application, zkClient), String.class);

        // 支持遮盖，字符偏移，groovy脚本三种方式脱敏
        if (CommonConfig.COVER_TYPE.equals(type)) {
            return coverDesensitize(recorder, requestBody, application);
        }
        if (CommonConfig.OFFSET_TYPE.equals(type)) {
            return offsetDesensitize(recorder, requestBody, application);
        }
        if (CommonConfig.GROOVY_TYPE.equals(type)) {
            return groovyDesensitize(recorder, application);
        }
        return recorder;
    }

    // 遮盖脱敏流程
    public Recorder coverDesensitize(Recorder recorder, JSONObject requestBody, String application) throws Exception {
        String methodRegexData = ZookeeperUtil.getData(CommonConfig.PROJECT_NODE + CommonConfig.DESENSITIZE_NODE
                + CommonConfig.SLASH + application + CommonConfig.SLASH + recorder.getMethodName(), zkClient);

        JSONArray array = requestBody.getJSONArray(CommonConfig.ARGUMENTS_FIELD);
        String requestResult = array.toString();
        String responseResult = recorder.getResponseBody();

        // method 层级脱敏规则
        if (StringUtils.isNotBlank(methodRegexData)) {
            HashMap<String, ReplaceRegexEntity> map = JSON.parseObject(methodRegexData,
                    new TypeReference<HashMap<String, ReplaceRegexEntity>>() {
                    });

            for (String key : map.keySet()) {
                Pattern pattern = Pattern.compile(key);
                Matcher reqMatcher = pattern.matcher(requestResult);

                // 先匹配，再替换
                while (reqMatcher.find()) {
                    String after = reqMatcher.group().replaceAll(map.get(key).getRegex(), map.get(key).getSymbol());
                    requestResult = requestResult.replaceFirst(key, after);
                }
                Matcher respMatcher = pattern.matcher(responseResult);
                while (respMatcher.find()) {
                    String after = respMatcher.group().replaceAll(map.get(key).getRegex(), map.get(key).getSymbol());
                    responseResult = responseResult.replaceFirst(key, after);
                }
            }
        }

        String appRegexData = ZookeeperUtil.getData(CommonConfig.PROJECT_NODE + CommonConfig.DESENSITIZE_NODE
                + CommonConfig.SLASH + application + CommonConfig.GENERAL_NODE, zkClient);

        // application 层级脱敏规则
        if (StringUtils.isNotBlank(appRegexData)) {
            HashMap<String, ReplaceRegexEntity> map = JSON.parseObject(appRegexData,
                    new TypeReference<HashMap<String, ReplaceRegexEntity>>() {
                    });

            for (String key : map.keySet()) {
                Pattern pattern = Pattern.compile(key);
                Matcher reqMatcher = pattern.matcher(requestResult);
                while (reqMatcher.find()) {
                    String after = reqMatcher.group().replaceAll(map.get(key).getRegex(), map.get(key).getSymbol());
                    requestResult = requestResult.replaceFirst(key, after);
                }
                Matcher respMatcher = pattern.matcher(responseResult);
                while (respMatcher.find()) {
                    String after = respMatcher.group().replaceAll(map.get(key).getRegex(), map.get(key).getSymbol());
                    responseResult = responseResult.replaceFirst(key, after);
                }
            }
        }

        requestBody.put(CommonConfig.ARGUMENTS_FIELD, JSON.parseArray(requestResult));
        recorder.setRequestBody(requestBody.toJSONString());
        recorder.setResponseBody(responseResult);
        return recorder;
    }

    // groovy脚本脱敏流程
    public Recorder groovyDesensitize(Recorder recorder, String application) throws Exception {
        String data = ZookeeperUtil.getData(CommonConfig.PROJECT_NODE + CommonConfig.DESENSITIZE_NODE
                + CommonConfig.SLASH + application + CommonConfig.GENERAL_NODE, zkClient);
        GroovyInfoEntity groovyInfoEntity = JSON.parseObject(data, GroovyInfoEntity.class);

        // 加载脚本并调用脚本中的脱敏函数
        GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine(groovyInfoEntity.getUrl());
        Class scriptClass = groovyScriptEngine.loadScriptByName(groovyInfoEntity.getScriptName());
        GroovyObject scriptInstance = (GroovyObject) scriptClass.newInstance();
        String ret = (String) scriptInstance.invokeMethod(groovyInfoEntity.getFunctionName(),
                JSON.toJSONString(recorder));
        return JSON.parseObject(ret, Recorder.class);
    }

    // 字符串偏移脱敏流程
    public Recorder offsetDesensitize(Recorder recorder, JSONObject requestBody, String application) throws Exception {
        String methodRegexData = ZookeeperUtil.getData(CommonConfig.PROJECT_NODE + CommonConfig.DESENSITIZE_NODE
                + CommonConfig.SLASH + application + CommonConfig.SLASH + recorder.getMethodName(), zkClient);

        JSONArray array = requestBody.getJSONArray(CommonConfig.ARGUMENTS_FIELD);
        String requestResult = array.toString();
        String responseResult = recorder.getResponseBody();

        // method 层级脱敏规则
        if (StringUtils.isNotBlank(methodRegexData)) {
            HashMap<String, Integer> map = JSON.parseObject(methodRegexData,
                    new TypeReference<HashMap<String, Integer>>() { });
            for (String key : map.keySet()) {
                Pattern pattern = Pattern.compile(key);
                Matcher reqMatcher = pattern.matcher(requestResult);

                // 先匹配，再处理
                while (reqMatcher.find()) {
                    String after = offsetProcess(reqMatcher.group(), map.get(key));
                    requestResult = requestResult.replaceFirst(key, after);
                }
                Matcher respMatcher = pattern.matcher(responseResult);
                while (respMatcher.find()) {
                    String after = offsetProcess(respMatcher.group(), map.get(key));
                    responseResult = responseResult.replaceFirst(key, after);
                }
            }
        }

        String appRegexData = ZookeeperUtil.getData(CommonConfig.PROJECT_NODE + CommonConfig.DESENSITIZE_NODE
                + CommonConfig.SLASH + application + CommonConfig.GENERAL_NODE, zkClient);

        // application 层级脱敏规则
        if (StringUtils.isNotBlank(appRegexData)) {
            HashMap<String, Integer> map = JSON.parseObject(appRegexData,
                    new TypeReference<HashMap<String, Integer>>() { });
            for (String key : map.keySet()) {
                Pattern pattern = Pattern.compile(key);
                Matcher reqMatcher = pattern.matcher(requestResult);

                // 先匹配，再处理
                while (reqMatcher.find()) {
                    String after = offsetProcess(reqMatcher.group(), map.get(key));
                    requestResult = requestResult.replaceFirst(key, after);
                }
                Matcher respMatcher = pattern.matcher(responseResult);
                while (respMatcher.find()) {
                    String after = offsetProcess(respMatcher.group(), map.get(key));
                    responseResult = responseResult.replaceFirst(key, after);
                }
            }
        }

        requestBody.put(CommonConfig.ARGUMENTS_FIELD, JSON.parseArray(requestResult));
        recorder.setRequestBody(requestBody.toJSONString());
        recorder.setResponseBody(responseResult);
        return recorder;
    }

    private String offsetProcess(String str, Integer offset) {
        char[] chars = str.toCharArray();
        Random random = new Random();
        for (int i = 0; i < chars.length; i++) {
            int move;
            if (offset == null) {
                move = random.nextInt(100);
            } else {
                move = offset;
            }

            int code = (int) chars[i];

            // 0-9
            if (code >= 48 && code <= 57) {
                if (code + move % 10 > 57) {
                    chars[i] = (char) (code + move % 10 - 10);
                } else {
                    chars[i] = (char) (code + move % 10);
                }
            }

            // A-Z
            if (code >= 65 && code <= 90) {
                if (code + move % 26 > 90) {
                    chars[i] = (char) (code + move % 26 - 26);
                } else {
                    chars[i] = (char) (code + move % 26);
                }
            }

            // a-z
            if (code >= 97 && code <= 122) {
                if (code + move % 26 > 122) {
                    chars[i] = (char) (code + move % 26 - 26);
                } else {
                    chars[i] = (char) (code + move % 26);
                }
            }

            // 基本中文
            if (code >= 19968 && code <= 40869) {
                if (code + move % 20901 > 40869) {
                    chars[i] = (char) (code + move % 20901 - 20901);
                } else {
                    chars[i] = (char) (code + move % 20901);
                }
            }
        }
        return String.valueOf(chars);
    }
}
