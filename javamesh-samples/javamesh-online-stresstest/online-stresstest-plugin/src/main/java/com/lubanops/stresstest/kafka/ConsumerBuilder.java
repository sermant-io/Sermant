/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */
package com.lubanops.stresstest.kafka;

import com.lubanops.stresstest.core.Reflection;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.*;

import static com.lubanops.stresstest.config.Constant.SHADOW;

/**
 * Consumer builder
 *
 * @author yiwei
 * @since 2021/10/25
 */
public class ConsumerBuilder {
    /**
     * 创建测试consumer
     * @param originalConsumer 原始的kafka consumer
     * @param <K> Key
     * @param <V> Value
     * @return 影子kafka consumer
     */
    @SuppressWarnings("unchecked")
    public static <K, V> KafkaConsumer<K, V> initTestConsumer(KafkaConsumer<K, V> originalConsumer) {
        Properties props = new Properties();
        updateMetadata(props, originalConsumer);
        updateNormal(props, originalConsumer);
        updateClient(props, originalConsumer);
        updateCoordinator(props, originalConsumer);
        updateFetcher(props, originalConsumer);
        updateInterceptors(props, originalConsumer);
        updateSubscriptions(props, originalConsumer);
        Object keyObject = Reflection.getDeclaredValue("keyDeserializer", originalConsumer).orElse(null);
        Deserializer<K> keyDeserializer = null;
        if (keyObject instanceof Deserializer) {
            keyDeserializer = (Deserializer<K>) keyObject;
        }
        Object valueObject = Reflection.getDeclaredValue("valueDeserializer", originalConsumer).orElse(null);
        Deserializer<V> valueDeserializer = null;
        if (keyObject instanceof Deserializer) {
            valueDeserializer = (Deserializer<V>) valueObject;
        }
        return new KafkaConsumer<K, V>(props, keyDeserializer, valueDeserializer);
    }

    private static void updateNormal(Properties props, Object instance) {
        Reflection.getDeclaredValue("clientId", instance).ifPresent(clientId ->
                props.put(ConsumerConfig.CLIENT_ID_CONFIG, SHADOW + clientId));
        Reflection.getDeclaredValue("groupId", instance).ifPresent(groupId -> {
            Object result;
            if (groupId instanceof Optional && ((Optional<?>) groupId).isPresent()) {
                result = ((Optional<?>) groupId).get();
            } else {
                result = groupId;
            }
            props.put(ConsumerConfig.GROUP_ID_CONFIG, SHADOW + result);
        });
        Reflection.getDeclaredValue("retryBackoffMs", instance).ifPresent(retryBackoffMs ->
                props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs));
        Reflection.getDeclaredValue("requestTimeoutMs", instance).ifPresent(requestTimeoutMs -> {
            if (requestTimeoutMs instanceof Long) {
                props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, ((Long) requestTimeoutMs).intValue());
            }
        });
    }

    private static void updateSubscriptions(Properties props, Object instance) {
        Reflection.getDeclaredValue("subscriptions", instance).flatMap(subscriptions ->
                Reflection.getDeclaredValue("defaultResetStrategy", subscriptions)).ifPresent(defaultResetStrategy ->
                props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                        defaultResetStrategy.toString().toLowerCase(Locale.ROOT)));
    }

    private static void updateInterceptors(Properties props, Object instance) {
        Reflection.getDeclaredValue("interceptors", instance).flatMap(interceptors ->
                Reflection.getDeclaredValue("interceptors", interceptors)).ifPresent(list -> {
                    if (list instanceof List<?> && ((List<?>) list).size() > 0) {
                        List<Class<?>> classList = new ArrayList<>();
                        for (Object item : (List<?>) list) {
                            classList.add(item.getClass());
                        }
                        props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, classList.toString());
                    }
                });
    }

    private static void updateMetadata(Properties props, Object instance) {
        Reflection.getDeclaredValue("metadata", instance).ifPresent(metadata -> {
            updateProps(props, ConsumerConfig.METADATA_MAX_AGE_CONFIG, metadata, "metadataExpireMs");
            Reflection.getDeclaredValue("cache", metadata).flatMap(cache ->
                    Reflection.getDeclaredValue("nodes", cache)).ifPresent(nodes -> {
                if (nodes instanceof Map) {
                    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootStrapServers((Map<Integer, Node>) nodes));
                }
            });
        });
    }

    private static void updateClient(Properties props, Object instance) {
        Reflection.getDeclaredValue("client", instance).flatMap(client ->
                Reflection.getDeclaredValue("client", client))
                .ifPresent(kafkaClient -> {
                    updateProps(props, ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, kafkaClient, "reconnectBackoffMs");
                    updateProps(props, ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, kafkaClient,
                            "reconnectBackoffMax");
                    updateProps(props, ConsumerConfig.SEND_BUFFER_CONFIG, kafkaClient, "socketSendBuffer");
                    updateProps(props, ConsumerConfig.RECEIVE_BUFFER_CONFIG, kafkaClient, "socketReceiveBuffer");
                });
    }

    private static void updateFetcher(Properties props, Object instance) {
        Reflection.getDeclaredValue("fetcher", instance).ifPresent(fetcher -> {
            updateProps(props, ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetcher, "minBytes");
            updateProps(props, ConsumerConfig.FETCH_MAX_BYTES_CONFIG, fetcher, "maxBytes");
            updateProps(props, ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetcher, "maxWaitMs");
            updateProps(props, ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, fetcher, "fetchSize");
            updateProps(props, ConsumerConfig.MAX_POLL_RECORDS_CONFIG, fetcher, "maxPollRecords");
            updateProps(props, ConsumerConfig.CHECK_CRCS_CONFIG, fetcher, "checkCrcs");
        });
    }

    private static void updateCoordinator(Properties props, Object instance) {
        Reflection.getDeclaredValue("coordinator", instance).ifPresent(coordinator -> {
            updateProps(props, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, coordinator, "autoCommitEnabled");
            updateProps(props, ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, coordinator, "autoCommitIntervalMs");
            updateProps(props, ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG, coordinator, "excludeInternalTopics");
            updateProps(props, ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, coordinator, "sessionTimeoutMs");
        });
    }

    private static void updateProps(Properties props, String configName, Object instance, String fieldName) {
        Reflection.getDeclaredValue(fieldName, instance).ifPresent(value ->
                props.put(configName, value));
    }

    private static String getBootStrapServers(Map<Integer, Node> nodes) {
        StringBuilder builder = new StringBuilder();
        for (Node node : nodes.values()) {
            builder.append(node.host()).append(":").append(node.port()).append(",");
        }
        return builder.substring(0, builder.length() - 1);
    }
}
