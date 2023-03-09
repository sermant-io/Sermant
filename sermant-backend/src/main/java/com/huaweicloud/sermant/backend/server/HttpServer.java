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

package com.huaweicloud.sermant.backend.server;

import com.huaweicloud.sermant.backend.cache.HeartbeatCache;
import com.huaweicloud.sermant.backend.common.conf.CommonConst;
import com.huaweicloud.sermant.backend.common.conf.KafkaConf;
import com.huaweicloud.sermant.backend.dao.EventService;
import com.huaweicloud.sermant.backend.entity.Address;
import com.huaweicloud.sermant.backend.entity.AddressScope;
import com.huaweicloud.sermant.backend.entity.AddressType;
import com.huaweicloud.sermant.backend.entity.AgentInfo;
import com.huaweicloud.sermant.backend.entity.EventLevel;
import com.huaweicloud.sermant.backend.entity.EventsRequestEntity;
import com.huaweicloud.sermant.backend.entity.EventsResponseEntity;
import com.huaweicloud.sermant.backend.entity.HeartBeatResult;
import com.huaweicloud.sermant.backend.entity.HeartbeatEntity;
import com.huaweicloud.sermant.backend.entity.MonitorItem;
import com.huaweicloud.sermant.backend.entity.Protocol;
import com.huaweicloud.sermant.backend.entity.PublishConfigEntity;
import com.huaweicloud.sermant.backend.entity.QueryResultEventInfoEntity;
import com.huaweicloud.sermant.backend.entity.RegisterResult;
import com.huaweicloud.sermant.backend.entity.WebhooksIdRequestEntity;
import com.huaweicloud.sermant.backend.entity.WebhooksResponseEntity;
import com.huaweicloud.sermant.backend.kafka.KafkaConsumerManager;
import com.huaweicloud.sermant.backend.service.dynamicconfig.DynamicConfigurationFactoryServiceImpl;
import com.huaweicloud.sermant.backend.service.dynamicconfig.service.DynamicConfigurationService;
import com.huaweicloud.sermant.backend.service.dynamicconfig.utils.LabelGroupUtils;
import com.huaweicloud.sermant.backend.util.DateUtil;
import com.huaweicloud.sermant.backend.util.RandomUtil;
import com.huaweicloud.sermant.backend.util.UuidUtil;
import com.huaweicloud.sermant.backend.webhook.EventPushHandler;
import com.huaweicloud.sermant.backend.webhook.WebHookClient;
import com.huaweicloud.sermant.backend.webhook.WebHookConfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private static final String SUCCESS = "success";
    private static final String FAILED = "failed";
    private static final int DEFAULT_IP_INDEX = 0;
    private static final int NULL_IP_LENGTH = 0;
    private static final int MAX = 10;

    @Autowired
    private KafkaConf conf;

    @Autowired
    private DynamicConfigurationFactoryServiceImpl dynamicConfigurationFactoryService;

    private EventPushHandler eventPushHandler = new EventPushHandler();

    private final RandomUtil randomUtil = new RandomUtil();

    private EventService eventService = EventService.getInstance();

    private long randomLong = UuidUtil.getId();
    private final int randomInt = randomUtil.getRandomInt(MAX);
    private final String randomStr = randomUtil.getRandomStr(MAX);


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

    /**
     * 查询事件
     *
     * @param eventsRequestEntity 查询时间请求实体
     * @return 查询结果
     */
    @PostMapping("/event/events")
    public EventsResponseEntity queryEvent(@RequestBody(required = false) EventsRequestEntity eventsRequestEntity) {
        EventsResponseEntity eventsResponseEntity = new EventsResponseEntity();
        List<QueryResultEventInfoEntity> queryResult = eventService.queryEvent(eventsRequestEntity);
        setEventCount(eventsResponseEntity, queryResult);
        eventsResponseEntity.setTotal(queryResult.size());
        eventsResponseEntity.setQueryResultEventInfoEntities(queryResult);
        eventsResponseEntity.setPageSize(CommonConst.DEFAULT_PAGE_SIZE);
        eventsResponseEntity.setPageNum(queryResult.size() / CommonConst.DEFAULT_PAGE_SIZE + 1);
        return eventsResponseEntity;
    }

    /**
     * 查询webhook
     *
     * @return webhook信息
     */
    @GetMapping("/event/webhooks")
    public WebhooksResponseEntity getWebhooks() {
        WebhooksResponseEntity webhooksResponseEntity = new WebhooksResponseEntity();
        List<WebHookClient> webHookClients = eventPushHandler.getWebHookClients();
        webhooksResponseEntity.setTotal(webHookClients.size());
        List<WebHookConfig> webHookConfigs = new ArrayList<>();
        for (WebHookClient webHookClient : webHookClients) {
            webHookConfigs.add(webHookClient.getConfig());
        }
        webhooksResponseEntity.setWebhooks(webHookConfigs);
        return webhooksResponseEntity;
    }

    /**
     * 配置webhook
     *
     * @param webhooksIdRequestEntity webhook配置请求实体
     * @param id                      webhook id
     * @return 配置结果
     */
    @PostMapping("/event/webhooks/{id}")
    public boolean setWebhook(@RequestBody(required = false) WebhooksIdRequestEntity webhooksIdRequestEntity,
                              @PathVariable String id) {
        List<WebHookClient> webHookClients = eventPushHandler.getWebHookClients();
        for (WebHookClient webHookClient : webHookClients) {
            WebHookConfig config = webHookClient.getConfig();
            if (id.equals(config.getId().toString())) {
                config.setUrl(webhooksIdRequestEntity.getUrl());
                config.setEnable(webhooksIdRequestEntity.isEnable());
            }
        }
        return true;
    }

    private void setEventCount(EventsResponseEntity eventsResponseEntity, List<QueryResultEventInfoEntity> queryResult) {
        int emergencyNum = 0;
        int importantNum = 0;
        int normalNum = 0;
        for (QueryResultEventInfoEntity q : queryResult) {
            if (q.getEventInfoEntity().getLevel().equals(EventLevel.EMERGENCY)) {
                emergencyNum += 1;
                continue;
            }
            if (q.getEventInfoEntity().getLevel().equals(EventLevel.IMPORTANT)) {
                importantNum += 1;
                continue;
            }
            normalNum += 1;
        }
        eventsResponseEntity.setEmergency(emergencyNum);
        eventsResponseEntity.setImportant(importantNum);
        eventsResponseEntity.setNormal(normalNum);
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
        address.setType(AddressType.ACCESS);
        address.setScope(AddressScope.OUTER);
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
        Map<String, HeartbeatEntity> heartbeatMessages = HeartbeatCache.getHeartbeatMessages();
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
            agentInfo.setAppName(heartbeatEntity.getApp());
            agentMap.put(instanceId, agentInfo);
        }
        if (agentMap.get(instanceId) != null && heartbeatEntity.getPluginName() != null) {
            AgentInfo agentInfo = agentMap.get(instanceId);
            Map<String, String> pluginMap = agentInfo.getPluginsMap();
            pluginMap.put(heartbeatEntity.getPluginName(), heartbeatEntity.getPluginVersion());
            agentInfo.setPluginsMap(pluginMap);
            agentInfo.setLastHeartbeatTime(DateUtil.getFormatDate(heartbeatEntity.getLastHeartbeat()));
            agentInfo.setHeartbeatTime(DateUtil.getFormatDate(heartbeatEntity.getHeartbeatVersion()));
        }
    }
}
