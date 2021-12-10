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

package com.huawei.javamesh.backend.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huawei.javamesh.backend.common.conf.KafkaConf;
import com.huawei.javamesh.backend.entity.Address;
import com.huawei.javamesh.backend.entity.AddressScope;
import com.huawei.javamesh.backend.entity.AddressType;
import com.huawei.javamesh.backend.entity.AgentInfo;
import com.huawei.javamesh.backend.entity.HeartBeatResult;
import com.huawei.javamesh.backend.entity.MonitorItem;
import com.huawei.javamesh.backend.entity.PluginInfo;
import com.huawei.javamesh.backend.entity.Protocol;
import com.huawei.javamesh.backend.entity.PublishConfigEntity;
import com.huawei.javamesh.backend.entity.RegisterResult;
import com.huawei.javamesh.backend.kafka.KafkaConsumerManager;
import com.huawei.javamesh.backend.service.dynamicconfig.DynamicConfigurationFactoryServiceImpl;
import com.huawei.javamesh.backend.service.dynamicconfig.service.DynamicConfigurationService;
import com.huawei.javamesh.backend.service.dynamicconfig.utils.LabelGroupUtils;
import com.huawei.javamesh.backend.util.RandomUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Component
@RestController
@RequestMapping("/sermant")
public class HttpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    @Autowired
    private KafkaConf conf;

    @Autowired
    private DynamicConfigurationFactoryServiceImpl dynamicConfigurationFactoryService;

    private final String DEFAULT_AGENT_NAME = "sermant";
    private final String DEFAULT_PLUGIN_VERSION = "unknown";
    private final String SUCCESS = "success";
    private final String FAILED = "failed";

    RandomUtil RANDOM_UTIL = new RandomUtil();
    private final Integer MIN = 1;
    private final Integer MAX = 10;

    private final Long random_long = RANDOM_UTIL.getRandomLong(MIN, MAX);
    private final Integer random_int = RANDOM_UTIL.getRandomInt(MAX);
    private final String random_str = RANDOM_UTIL.getRandomStr(MAX);


    @PostMapping("/master/v1/register")
    public String invokePost(@RequestBody JSONObject jsonParam) {
        RegisterResult registerResult = new RegisterResult();
        registerResult.setAppId(random_long);
        registerResult.setEnvId(random_long);
        registerResult.setDomainId(random_int);
        registerResult.setAgentVersion(random_str);
        registerResult.setInstanceId(random_long);
        registerResult.setBusinessId(random_long);
        return JSONObject.toJSONString(registerResult);
    }

    @PostMapping("/master/v1/heartbeat")
    public String invokePost() {

        Hashtable<String, String> map = new Hashtable<>();

        HeartBeatResult heartBeatResult = new HeartBeatResult();
        heartBeatResult.setHeartBeatInterval(random_int);
        heartBeatResult.setAttachment(map);
        heartBeatResult.setMonitorItemList(Collections.singletonList(getMonitorItem(map)));
        heartBeatResult.setSystemProperties(map);
        heartBeatResult.setAccessAddressList(Collections.singletonList(getAddress()));
        heartBeatResult.setInstanceStatus(random_int);
        heartBeatResult.setBusinessId(random_long);
        heartBeatResult.setMd5(random_str);
        return JSONObject.toJSONString(heartBeatResult);
    }

    @GetMapping("/getPluginsInfo")
    public String invokeGet() {
        ConsumerRecords<String, String> consumerRecords = getHeartbeatInfo();
        AgentInfo agentInfo = new AgentInfo();
        Map<String, PluginInfo> pluginCache = new HashMap<String, PluginInfo>();
        for (ConsumerRecord<String, String> record : consumerRecords) {
            HashMap hashMap = JSON.parseObject(record.value(), HashMap.class);
            agentInfo.setIp(hashMap.get("ip"));
            agentInfo.setHeartbeatTime(hashMap.get("heartbeatVersion"));
            agentInfo.setLastHeartbeatTime(hashMap.get("lastHeartbeat"));
            agentInfo.setVersion(hashMap.get("version"));
            String name = (String) hashMap.getOrDefault("pluginName", DEFAULT_AGENT_NAME);
            if (!name.equals(DEFAULT_AGENT_NAME)) {
                PluginInfo pluginInfo = new PluginInfo();
                pluginInfo.setName(name);
                pluginInfo.setVersion((String) hashMap.getOrDefault("pluginVersion", DEFAULT_PLUGIN_VERSION));
                pluginCache.put(name, pluginInfo);
            }
        }
        agentInfo.setPluginsInfos(new ArrayList<>(pluginCache.values()));
        return JSONObject.toJSONString(agentInfo);
    }

    @PostMapping("/publishConfig")
    public String invokePost(@RequestBody PublishConfigEntity publishConfig) {
        try {
            DynamicConfigurationService dcs = dynamicConfigurationFactoryService.getDynamicConfigurationService();
            dcs.publishConfig(publishConfig.getKey(),
                    LabelGroupUtils.createLabelGroup(publishConfig.getGroup()),
                    publishConfig.getContent());
            return SUCCESS;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return FAILED;
    }

    public MonitorItem getMonitorItem(Hashtable<String, String> map) {
        MonitorItem monitorItem = new MonitorItem();
        monitorItem.setCollectorName(random_str);
        monitorItem.setInterval(random_int);
        monitorItem.setCollectorId(random_int);
        monitorItem.setMonitorItemId(random_long);
        monitorItem.setStatus(random_int);
        monitorItem.setParameters(map);
        return monitorItem;
    }

    public Address getAddress() {
        Address address = new Address();
        address.setHost(random_str);
        address.setPort(random_int);
        address.setSport(random_int);
        address.setType(AddressType.access);
        address.setScope(AddressScope.outer);
        address.setProtocol(Protocol.WS);
        return address;
    }

    private ConsumerRecords<String, String> getHeartbeatInfo() {
        ConsumerRecords<String, String> consumerRecords = null;
        try {
            KafkaConsumer<String, String> consumer = KafkaConsumerManager.getInstance(conf).getConsumer();
            consumer.subscribe(Arrays.asList(conf.getTopicHeartBeat()));
            ConsumerRecords<String, String> records = consumer.poll(conf.getKafkaPoolTimeoutMs());
            consumerRecords = records;
        } catch (Exception e) {
            LOGGER.error("getHeartbeatInfo failed");
        }
        return consumerRecords;
    }
}
