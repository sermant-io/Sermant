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

package com.huawei.flowcontrol.console.discovery;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.huawei.flowcontrol.console.entity.AppInfo;
import com.huawei.flowcontrol.console.entity.MachineInfo;
import com.huawei.flowcontrol.console.repository.metric.MachineDiscovery;
import com.huawei.flowcontrol.console.util.DataType;
import com.huawei.flowcontrol.console.util.RedisUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * redis存储心跳数据
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
@Component
public class RedisMachineDiscovery implements MachineDiscovery {
    /**
     * redis存储应用名
     */
    private static final String APP_NAMES = DataType.APPNAMES.getDataType();

    /**
     * 心跳数据
     */
    private static final String HEARTBEAT_DATA = DataType.HEARTBEAT_DATA.getDataType();

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisMachineDiscovery.class);

    private String message = "app name cannot be blank";

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public long addMachine(MachineInfo machineInfo) {
        AssertUtil.notNull(machineInfo, "machineInfo cannot be null");
        redisUtil.setSet(APP_NAMES, machineInfo.getApp());
        redisUtil.setHash(machineInfo.getApp() + DataType.SEPARATOR_COLON.getDataType()
            + HEARTBEAT_DATA, machineInfo.getIp(), JSONObject.toJSONString(machineInfo), true);
        return 1;
    }

    @Override
    public boolean removeMachine(String app, String ip, int port) {
        AssertUtil.assertNotBlank(app, message);
        Object appInfoString = redisUtil.queryForValue(app
            + DataType.SEPARATOR_COLON.getDataType() + HEARTBEAT_DATA, ip);
        if (appInfoString != null) {
            redisUtil.delHash(app + DataType.SEPARATOR_COLON.getDataType() + HEARTBEAT_DATA, ip);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getAppNames() {
        return new ArrayList<>(redisUtil.getSet(APP_NAMES));
    }

    @Override
    public AppInfo getDetailApp(String app) {
        AssertUtil.assertNotBlank(app, message);
        return stringToAppInfo(app);
    }

    @Override
    public Set<AppInfo> getBriefApps() {
        Set<AppInfo> appInfos = new HashSet<>();

        // 查询应用名
        Set<String> app = redisUtil.getSet(APP_NAMES);
        for (String object : app) {
            appInfos.add(stringToAppInfo(object));
        }
        return appInfos;
    }

    @Override
    public void removeApp(String app) {
        AssertUtil.assertNotBlank(app, message);
        LOGGER.info("delete app:{}", app);

        // 删除机器数据 & 应用名
        redisUtil.delKey(app + DataType.SEPARATOR_COLON.getDataType() + HEARTBEAT_DATA);
        redisUtil.delSet(APP_NAMES, app);
    }

    /**
     * appinfo转换
     *
     * @param app 应用名
     * @return 返回app信息
     */
    private AppInfo stringToAppInfo(String app) {
        AppInfo appInfo = new AppInfo();

        // 获取redis数据
        List<String> list = redisUtil.getHashValues(app + DataType.SEPARATOR_COLON.getDataType() + HEARTBEAT_DATA);
        if ((list != null) && (list.size() > 0)) {
            for (String object : list) {
                try {
                    MachineInfo machine = JSONObject.parseObject(object, MachineInfo.class);
                    if (StringUtils.isEmpty(appInfo.getApp())) {
                        // 设置app & apptype
                        appInfo.setApp(machine.getApp());
                        appInfo.setAppType(machine.getAppType());
                    }
                    appInfo.addMachine(machine);
                } catch (JSONException e) {
                    LOGGER.error("Failed to convert MachineInfo！", e.getMessage());
                }
            }
        }
        return appInfo;
    }
}
