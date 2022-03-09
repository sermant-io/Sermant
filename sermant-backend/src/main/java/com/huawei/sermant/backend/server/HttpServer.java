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

package com.huawei.sermant.backend.server;

import com.huawei.sermant.backend.cache.HeartbeatCache;
import com.huawei.sermant.backend.common.conf.KafkaConf;
import com.huawei.sermant.backend.entity.Address;
import com.huawei.sermant.backend.entity.AddressScope;
import com.huawei.sermant.backend.entity.AddressType;
import com.huawei.sermant.backend.entity.AgentInfo;
import com.huawei.sermant.backend.entity.HeartBeatResult;
import com.huawei.sermant.backend.entity.HeartbeatEntity;
import com.huawei.sermant.backend.entity.MonitorItem;
import com.huawei.sermant.backend.entity.Protocol;
import com.huawei.sermant.backend.entity.PublishConfigEntity;
import com.huawei.sermant.backend.entity.RegisterResult;
import com.huawei.sermant.backend.kafka.KafkaConsumerManager;
import com.huawei.sermant.backend.service.dynamicconfig.DynamicConfigurationFactoryServiceImpl;
import com.huawei.sermant.backend.service.dynamicconfig.service.DynamicConfigurationService;
import com.huawei.sermant.backend.service.dynamicconfig.utils.LabelGroupUtils;
import com.huawei.sermant.backend.util.DateUtil;
import com.huawei.sermant.backend.util.RandomUtil;
import com.huawei.sermant.backend.util.UuidUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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
import java.util.List;
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

    private final String SUCCESS = "success";
    private final String FAILED = "failed";
    private final Integer DEFAULT_IP_INDEX = 0;
    private final Integer NULL_IP_LENGTH = 0;

    RandomUtil RANDOM_UTIL = new RandomUtil();
    DateUtil DATE_UTIL = new DateUtil();
    private final Integer MAX = 10;

    private long randomLong = UuidUtil.getId();
    private final int randomInt = RANDOM_UTIL.getRandomInt(MAX);
    private final String randomStr = RANDOM_UTIL.getRandomStr(MAX);


    @PostMapping("/master/v1/register")
    public String invokePost(@RequestBody JSONObject jsonParam) {
        long instanceId = UuidUtil.getId();
        RegisterResult registerResult = new RegisterResult();
        registerResult.setAppId(randomLong);
        registerResult.setEnvId(randomLong);
        registerResult.setDomainId(randomInt);
        registerResult.setAgentVersion(randomStr);
        registerResult.setInstanceId(instanceId);
        registerResult.setBusinessId(randomLong);
        return JSONObject.toJSONString(registerResult);
    }

    @PostMapping("/master/v1/heartbeat")
    public String invokePost() {

        Hashtable<String, String> map = new Hashtable<>();

        HeartBeatResult heartBeatResult = new HeartBeatResult();
        heartBeatResult.setHeartBeatInterval(randomInt);
        heartBeatResult.setAttachment(map);
        heartBeatResult.setMonitorItemList(Collections.singletonList(getMonitorItem(map)));
        heartBeatResult.setSystemProperties(map);
        heartBeatResult.setAccessAddressList(Collections.singletonList(getAddress()));
        heartBeatResult.setInstanceStatus(randomInt);
        heartBeatResult.setBusinessId(randomLong);
        heartBeatResult.setMd5(randomStr);
        return JSONObject.toJSONString(heartBeatResult);
    }

    @GetMapping("/getPluginsInfo")
    public String invokeGet() {
        if (Boolean.parseBoolean(conf.getIsHeartbeatCache())) {
            return JSONObject.toJSONString(getHeartbeatMessageCache());
        } else {
            ConsumerRecords<String, String> consumerRecords = getHeartbeatInfo();
            return JSONObject.toJSONString(getHeartbeatMessage(consumerRecords));
        }
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
        monitorItem.setCollectorName(randomStr);
        monitorItem.setInterval(randomInt);
        monitorItem.setCollectorId(randomInt);
        monitorItem.setMonitorItemId(randomLong);
        monitorItem.setStatus(randomInt);
        monitorItem.setParameters(map);
        return monitorItem;
    }

    public Address getAddress() {
        Address address = new Address();
        address.setHost(randomStr);
        address.setPort(randomInt);
        address.setSport(randomInt);
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

    private String pluginMapToStr(Map<String, String> map) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            result.append("\n").append(entry.getKey()).append("-").append(entry.getValue());
        }
        return result.toString();
    }

    private List<AgentInfo> getHeartbeatMessage(ConsumerRecords<String, String> consumerRecords) {
        Map<String, AgentInfo> agentMap = new HashMap<>();
        for (ConsumerRecord<String, String> record : consumerRecords) {
            HeartbeatEntity heartbeatEntity = JSON.parseObject(record.value(), HeartbeatEntity.class);
            setAgentInfo(agentMap, heartbeatEntity);
        }
        return new ArrayList<>(agentMap.values());
    }

    private List<AgentInfo> getHeartbeatMessageCache() {
        HashMap<String, HeartbeatEntity> heartbeatMessages = HeartbeatCache.getHeartbeatMessages();
        if (heartbeatMessages != null) {
            Map<String, AgentInfo> agentMap = new HashMap<>();
            for (HeartbeatEntity heartbeatEntity : heartbeatMessages.values()) {
                setAgentInfo(agentMap, heartbeatEntity);
            }
            return new ArrayList<>(agentMap.values());
        } else {
            return new ArrayList<>();
        }
    }

    private void setAgentInfo(Map<String, AgentInfo> agentMap, HeartbeatEntity heartbeatEntity) {
        List<String> ips = heartbeatEntity.getIp();
        String instanceId = heartbeatEntity.getInstanceId();
        if (ips == null || ips.size() == NULL_IP_LENGTH) {
            return;
        }
        if (agentMap.get(instanceId) == null) {
            String ip = ips.get(DEFAULT_IP_INDEX);
            AgentInfo agentInfo = new AgentInfo();
            agentInfo.setIp(ip);
            agentInfo.setVersion(heartbeatEntity.getVersion());
            agentInfo.setPluginsMap(new HashMap<String, String>());
            agentInfo.setInstanceId(instanceId);
            agentMap.put(instanceId, agentInfo);
        }
        if (agentMap.get(instanceId) != null && heartbeatEntity.getPluginName() != null) {
            AgentInfo agentInfo = agentMap.get(instanceId);
            Map<String, String> pluginMap = agentInfo.getPluginsMap();
            pluginMap.put(heartbeatEntity.getPluginName(), heartbeatEntity.getPluginVersion());
            agentInfo.setPluginsMap(pluginMap);
            agentInfo.setLastHeartbeatTime(DATE_UTIL.getFormatDate(heartbeatEntity.getLastHeartbeat()));
            agentInfo.setHeartbeatTime(DATE_UTIL.getFormatDate(heartbeatEntity.getHeartbeatVersion()));
        }
    }
}
