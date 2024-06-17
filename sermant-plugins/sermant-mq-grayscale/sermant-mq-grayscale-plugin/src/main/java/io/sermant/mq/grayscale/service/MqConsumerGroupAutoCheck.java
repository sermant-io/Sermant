/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.mq.grayscale.service;

import io.sermant.core.common.LoggerFactory;
import io.sermant.mq.grayscale.utils.MqGrayscaleConfigUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.MQClientAPIImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.protocol.body.GroupList;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * consumer group auto check service
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class MqConsumerGroupAutoCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    private static final String CONSUME_TYPE_AUTO = "auto";

    private static final long CONNECT_TIMEOUT = 5000L;

    private static MQClientInstance mqClientInstance = null;

    private static String baseOriginGroup = null;

    private static String topic = null;

    private static HashSet<String> lastAvailableGroup = new HashSet<>();

    static {
        EXECUTOR_SERVICE.scheduleWithFixedDelay(
                MqConsumerGroupAutoCheck::schedulerCheckGrayConsumerStart, 0L,
                MqGrayscaleConfigUtils.getAutoCheckDelayTime(), TimeUnit.SECONDS);
    }

    private MqConsumerGroupAutoCheck() {
    }

    /**
     * set mqClientInstance info
     *
     * @param instance MQClientInstance
     */
    public static void setMqClientInstance(MQClientInstance instance) {
        mqClientInstance = instance;
    }

    /**
     * set base originGroup info
     *
     * @param originGroup originGroup
     */
    public static void setOriginGroup(String originGroup) {
        baseOriginGroup = originGroup;
    }

    /**
     * set topic info
     *
     * @param topic topic
     */
    public static void setTopic(String topic) {
        MqConsumerGroupAutoCheck.topic = topic;
    }

    /**
     * scheduler check environment is hava gray consumer consume message
     */
    public static void schedulerCheckGrayConsumerStart() {
        if (topic == null || mqClientInstance == null) {
            return;
        }
        if (!CONSUME_TYPE_AUTO.equals(MqGrayscaleConfigUtils.getConsumeType())) {
            return;
        }
        try {
            MQClientAPIImpl mqClientApi = mqClientInstance.getMQClientAPIImpl();
            TopicRouteData topicRouteData = mqClientApi.getTopicRouteInfoFromNameServer(topic, CONNECT_TIMEOUT, false);
            List<String> brokerList = new ArrayList<>();
            for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
                brokerList.addAll(brokerData.getBrokerAddrs().values());
            }
            String brokerAddress = brokerList.get(0);
            Set<String> availableGroup = new HashSet<>();
            GroupList groupList = mqClientApi.queryTopicConsumeByWho(brokerAddress, topic, CONNECT_TIMEOUT);
            LOGGER.warning(String.format(Locale.ENGLISH, "auto check gray consumer, fined groups: %s",
                    groupList.getGroupList()));
            for (String group : groupList.getGroupList()) {
                try {
                    List<String> consumerIds = mqClientApi.getConsumerIdListByGroup(brokerAddress, group,
                            CONNECT_TIMEOUT);
                    LOGGER.warning(String.format(Locale.ENGLISH, "auto check gray consumer, current group: %s, "
                                    + "contains consumerIds: %s", group, consumerIds));
                    if (!consumerIds.isEmpty()) {
                        availableGroup.add(group);
                    }
                } catch (RemotingConnectException | RemotingSendRequestException | RemotingTimeoutException
                         | MQBrokerException | InterruptedException e) {
                    LOGGER.log(Level.FINE, String.format(Locale.ENGLISH, "auto check gray consumer, get consumerIds "
                            + "error, group: %s", group), e);
                }
            }
            modifyConsumerExcludeTags(availableGroup);
        } catch (MQClientException | InterruptedException | RemotingTimeoutException | RemotingSendRequestException
                 | RemotingConnectException | MQBrokerException e) {
            LOGGER.log(Level.FINE, String.format(Locale.ENGLISH, "auto check gray consumer error, message: %s",
                    e.getMessage()), e);
        }
    }

    private static void modifyConsumerExcludeTags(Set<String> availableGroup) {
        HashSet<String> currentGroups = new HashSet<>(availableGroup);
        HashSet<String> lastGroups = new HashSet<>(lastAvailableGroup);
        currentGroups.removeAll(lastAvailableGroup);
        lastGroups.removeAll(availableGroup);
        if (!currentGroups.isEmpty() || !lastGroups.isEmpty()) {
            Set<String> excludeTag = new HashSet<>();
            for (String group : availableGroup) {
                if (!group.equals(baseOriginGroup)) {
                    String env = StringUtils.substringAfterLast(group, baseOriginGroup + "_");
                    if (StringUtils.isNotEmpty(env)) {
                        excludeTag.add(env);
                    }
                }
            }
            LOGGER.warning(String.format(Locale.ENGLISH, "auto check gray consumer, current lastAvailableGroup: %s",
                    lastAvailableGroup));
            lastAvailableGroup = new HashSet<>(availableGroup);
            MqGrayscaleConfigUtils.modifyExcludeTags(excludeTag);
        }
    }
}
