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

package io.sermant.mq.grayscale.rocketmq.service;

import io.sermant.core.common.LoggerFactory;
import io.sermant.mq.grayscale.config.ConsumeModeEnum;
import io.sermant.mq.grayscale.config.GrayTagItem;
import io.sermant.mq.grayscale.config.MqGrayConfigCache;
import io.sermant.mq.grayscale.rocketmq.config.RocketMqConsumerClientConfig;
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqGrayscaleConfigUtils;
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqReflectUtils;
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqSubscriptionDataUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.MQClientAPIImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * consumer group auto check service
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class RocketMqConsumerGroupAutoCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    /**
     * gray consumer tags at last time
     * key: namesrvAddr@topic@consumerGroup
     * value: consumer gray tags
     */
    private static final Map<String, Set<String>> LAST_TOPIC_GROUP_GRAY_TAG = new HashMap<>();

    /**
     * client configs for query consumer group
     * key: namesrvAddr@topic@consumerGroup
     * value: client config
     */
    private static final Map<String, RocketMqConsumerClientConfig> CONSUMER_CLIENT_CONFIG_MAP = new HashMap<>();

    private static final AtomicBoolean START_AUTO_CHECK = new AtomicBoolean(false);

    private static final long INITIAL_DELAY = 10L;

    private static final long ROCKET_MQ_READ_TIMEOUT = 5000L;

    private RocketMqConsumerGroupAutoCheck() {
    }

    /**
     * set current client MQClientInstance info
     *
     * @param topic topic
     * @param consumerGroup consumerGroup
     * @param mqClientInstance mqClientInstance
     */
    public static void setMqClientInstance(String topic, String consumerGroup, MQClientInstance mqClientInstance) {
        String subscribeScope = RocketMqSubscriptionDataUtils.buildSubscribeScope(topic, consumerGroup,
                mqClientInstance.getClientConfig().getNamesrvAddr());
        RocketMqConsumerClientConfig currentConfig = CONSUMER_CLIENT_CONFIG_MAP.get(subscribeScope);
        if (currentConfig != null && currentConfig.getMqClientInstance() == null) {
            currentConfig.setMqClientInstance(mqClientInstance);
        }
    }

    /**
     * sync check gray consumer group is running and update gray tags
     */
    public static void syncUpdateCacheGrayTags() {
        if (RocketMqGrayscaleConfigUtils.getConsumeType() == ConsumeModeEnum.AUTO && !START_AUTO_CHECK.get()) {
            // sync to obtain current gray consumer group at AUTO mode before start scheduler check group
            findGrayConsumerGroupAndUpdateGrayTags();
        }
    }

    /**
     * start scheduler check gray consumer is changed
     */
    public static void startSchedulerCheckGroupTask() {
        if (RocketMqGrayscaleConfigUtils.getConsumeType() == ConsumeModeEnum.AUTO) {
            if (START_AUTO_CHECK.compareAndSet(false, true)) {
                EXECUTOR_SERVICE.scheduleWithFixedDelay(
                        RocketMqConsumerGroupAutoCheck::findGrayConsumerGroupAndUpdateGrayTags, INITIAL_DELAY,
                        RocketMqGrayscaleConfigUtils.getAutoCheckDelayTime(), TimeUnit.SECONDS);
            }
        }
    }

    /**
     * find gray consumer group and update gray tags
     */
    public static void findGrayConsumerGroupAndUpdateGrayTags() {
        if (CONSUMER_CLIENT_CONFIG_MAP.isEmpty()) {
            return;
        }
        if (!StringUtils.isEmpty(RocketMqGrayscaleConfigUtils.getGrayGroupTag())) {
            return;
        }
        if (MqGrayConfigCache.getCacheConfig() == null
                || MqGrayConfigCache.getCacheConfig().getGrayscale().isEmpty()) {
            return;
        }
        for (RocketMqConsumerClientConfig clientConfig : CONSUMER_CLIENT_CONFIG_MAP.values()) {
            if (clientConfig.getMqClientInstance() == null) {
                continue;
            }
            Set<String> grayTags = findGrayConsumerGroupAndGetTags(clientConfig);
            LOGGER.log(Level.INFO, "[auto-check] current find gray tags: {0}.", grayTags);
            resetAutoCheckGrayTagItems(grayTags, clientConfig);
        }
    }

    /**
     * querying all consumer groups of Topic and Collecting grayGroupTag
     *
     * @param clientConfig clientConfig
     * @return grayTags
     */
    private static Set<String> findGrayConsumerGroupAndGetTags(RocketMqConsumerClientConfig clientConfig) {
        try {
            MQClientAPIImpl mqClientApi = clientConfig.getMqClientInstance().getMQClientAPIImpl();
            String brokerAddress = getBrokerAddress(clientConfig.getTopic(), mqClientApi);
            Object groupList = RocketMqReflectUtils.queryTopicConsumeByWho(mqClientApi, brokerAddress,
                    clientConfig.getTopic(), ROCKET_MQ_READ_TIMEOUT);
            return getGrayTagsByConsumerGroup(groupList, brokerAddress, mqClientApi,
                    clientConfig.getConsumerGroup());
        } catch (MQClientException | InterruptedException | RemotingTimeoutException | RemotingSendRequestException
                | RemotingConnectException e) {
            LOGGER.log(Level.FINE, String.format(Locale.ENGLISH, "[auto-check] error, message: %s",
                    e.getMessage()), e);
        }
        return new HashSet<>();
    }

    private static Set<String> getGrayTagsByConsumerGroup(Object groupList, String brokerAddress,
            MQClientAPIImpl mqClientApi, String consumerGroup) {
        Set<String> grayTags = new HashSet<>();
        for (String group : RocketMqReflectUtils.getGroupList(groupList)) {
            try {
                List<String> consumerIds = mqClientApi.getConsumerIdListByGroup(brokerAddress, group,
                        ROCKET_MQ_READ_TIMEOUT);
                if (consumerIds.isEmpty()) {
                    continue;
                }
                String grayTag = StringUtils.substringAfterLast(group, consumerGroup + "_");
                if (!StringUtils.isEmpty(grayTag)) {
                    grayTags.add(grayTag);
                }
            } catch (RemotingConnectException | RemotingSendRequestException | RemotingTimeoutException
                    | MQBrokerException | InterruptedException e) {
                LOGGER.warning(String.format(Locale.ENGLISH, "[auto-check] can not find ids in group: [%s].",
                        group));
            }
        }
        return grayTags;
    }

    private static String getBrokerAddress(String topic, MQClientAPIImpl mqClientApi)
            throws RemotingSendRequestException, RemotingConnectException, RemotingTimeoutException,
            InterruptedException, MQClientException {
        Object topicRouteData = RocketMqReflectUtils.getTopicRouteInfoFromNameServer(mqClientApi, topic,
                ROCKET_MQ_READ_TIMEOUT, false);
        List<String> brokerList = new ArrayList<>();
        for (Object brokerData : RocketMqReflectUtils.getBrokerDatas(topicRouteData)) {
            brokerList.addAll(RocketMqReflectUtils.getBrokerAddrs(brokerData).values());
        }

        // cluster mode has multiple addresses, just select one
        return brokerList.get(0);
    }

    /**
     * compare current query grayGroupTag with collected last time, reset autoCheckGrayTagItems
     *
     * @param grayTags grayTags
     * @param clientConfig MqConsumerClientConfig
     */
    private static void resetAutoCheckGrayTagItems(Set<String> grayTags, RocketMqConsumerClientConfig clientConfig) {
        String subscribeScope = RocketMqSubscriptionDataUtils.buildSubscribeScope(clientConfig.getTopic(),
                clientConfig.getConsumerGroup(), clientConfig.getAddress());
        if (grayTags.isEmpty()) {
            if (LAST_TOPIC_GROUP_GRAY_TAG.containsKey(subscribeScope)) {
                RocketMqSubscriptionDataUtils.resetAutoCheckGrayTagItems(new ArrayList<>(), clientConfig);
                LAST_TOPIC_GROUP_GRAY_TAG.remove(subscribeScope);
            }
            return;
        }
        if (isGrayTagsChanged(grayTags, subscribeScope)) {
            List<GrayTagItem> grayTagItems = new ArrayList<>();
            for (String grayTag : grayTags) {
                Optional<GrayTagItem> item = MqGrayConfigCache.getCacheConfig().getGrayTagByGroupTag(grayTag);
                item.ifPresent(grayTagItems::add);
            }
            LAST_TOPIC_GROUP_GRAY_TAG.put(subscribeScope, grayTags);
            RocketMqSubscriptionDataUtils.resetAutoCheckGrayTagItems(grayTagItems, clientConfig);
        }
    }

    private static boolean isGrayTagsChanged(Set<String> grayTags, String subscribeScope) {
        HashSet<String> currentGroups = new HashSet<>(grayTags);
        Set<String> lastTags = LAST_TOPIC_GROUP_GRAY_TAG.get(subscribeScope);
        if (LAST_TOPIC_GROUP_GRAY_TAG.containsKey(subscribeScope)) {
            currentGroups.removeAll(lastTags);
        }
        return !currentGroups.isEmpty() || grayTags.size() != lastTags.size();
    }

    /**
     * set consumer client config
     *
     * @param address address
     * @param topic topic
     * @param consumerGroup consumerGroup
     */
    public static void setConsumerClientConfig(String address, String topic, String consumerGroup) {
        RocketMqConsumerClientConfig config = new RocketMqConsumerClientConfig(address, topic, consumerGroup);
        String subscribeScope = RocketMqSubscriptionDataUtils.buildSubscribeScope(topic, consumerGroup, address);
        if (!CONSUMER_CLIENT_CONFIG_MAP.containsKey(subscribeScope)) {
            CONSUMER_CLIENT_CONFIG_MAP.put(subscribeScope, config);
        }
    }
}
