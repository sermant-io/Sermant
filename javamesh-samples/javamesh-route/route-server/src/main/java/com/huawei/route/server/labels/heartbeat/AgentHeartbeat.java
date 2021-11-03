/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.heartbeat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.huawei.route.server.console.util.RedisClient;
import com.huawei.route.server.labels.config.AgentHeartbeatConfig;
import com.huawei.route.server.labels.constant.LabelConstant;
import com.huawei.route.server.labels.label.service.LabelService;
import com.huawei.route.server.labels.vo.ScheduleLabelValidVo;
import com.huawei.route.server.rules.TagInstanceIpMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 处理从kafka中获取的agent心跳，存储到本地内存，如果超时，则从内存删除
 *
 * @author zhanghu
 * @since 2021-06-08
 */
@Component
public class AgentHeartbeat {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentHeartbeat.class);
    private static final String REDIS_KEY_PREFIX = LabelConstant.GENERAL_PAAS + LabelConstant.SEPARATOR;

    public static final String HEARTBEAT_HASH_KEY = REDIS_KEY_PREFIX + "heartbeat";

    private static final String EFFECT_FLAG = "effectFlag";

    private static final String LAST_HEARTBEAT = "lastHeartbeat";

    private static final String HEARTBEAT_REDIS_LOCK_KEY = REDIS_KEY_PREFIX + "heartbeat:lock";

    private static final String LABEL_TAKE_EFFECT_REDIS_LOCK_KEY = REDIS_KEY_PREFIX + "label:lock";

    /**
     * 初始化应用启动让标签生效的定时任务的延迟时间
     */
    private static final int INITIAL_DELAY = 5000;

    /**
     * 初始化应用启动让心跳的定时任务的延迟时间，这个延时要小于INITIAL_DELAY
     */
    private static final int HEARTBEAT_INITIAL_DELAY = INITIAL_DELAY - 3000;

    /**
     * 初始化应用启动让标签生效的定时任务执行的时间间隔
     */
    private static final int PERIOD = 10000;

    private static final int LABEL_TAKE_EFFECT_REDIS_LOCK_TIMEOUT = PERIOD - 1000;

    /**
     * 验证心跳的定时任务的执行时间间隔
     */
    private static final int HEARTBEAT_PERIOD = 5000;

    private static final int HEARTBEAT_REDIS_LOCK_TIMEOUT = HEARTBEAT_PERIOD - 1000;

    @Autowired
    private LabelService labelService;

    @Autowired
    private RedisClient redisUtil;

    @Autowired
    private TagInstanceIpMapper tagInstanceIpMapper;

    /**
     * 监听心跳
     *
     * @param msg 心跳信息
     */
    @KafkaListener(topics = "${heartbeat.topic:topic-heartbeat}")
    public void getHeartbeat(String msg) {
        JSONObject heartbeatMsg = JSON.parseObject(msg);
        if (!isLabelHeartbeat(heartbeatMsg)) {
            return;
        }
        String instanceName = heartbeatMsg.getString(LabelConstant.INSTANCE_NAME_MARKING);
        String serviceName = heartbeatMsg.getString(LabelConstant.SERVICE_NAME_MARKING);
        if (StringUtils.isNotEmpty(instanceName) && StringUtils.isNotEmpty(serviceName)) {
            JSONObject heartbeat = getServiceHeartbeat(serviceName, false);
            if (heartbeat.containsKey(instanceName)) {
                // 之前的心跳信息
                JSONObject instanceMsg = JSONObject.parseObject(heartbeat.getString(instanceName));

                // 标签是否生效的标记，跟之前的心跳信息保持一致就行，不需要更新
                heartbeatMsg.put(EFFECT_FLAG, instanceMsg.getBooleanValue(EFFECT_FLAG));
            } else {
                // 标签是否生效的标记，默认不生效
                heartbeatMsg.put(EFFECT_FLAG, false);
            }
            // 这里考虑到不同服务器的时间不一定一致，所以改收到消息的时间戳
            heartbeatMsg.put(LAST_HEARTBEAT, System.currentTimeMillis());
            heartbeat.put(instanceName, heartbeatMsg.toJSONString());
            setHeartbeat(serviceName, heartbeat);
        }
    }

    /**
     * 是否为标签库心跳
     * javamesh心跳根据name对各自插件的心跳进行区分，标签库有许多独有的数据, 需特别整理
     *
     * @param msg 心跳信息
     * @return 是否为标签库心跳
     */
    private boolean isLabelHeartbeat(JSONObject msg) {
        if (msg == null) {
            return false;
        }
        return StringUtils.equals(msg.getString("name"), LabelConstant.HEARTBEAT_NAME);
    }

    /**
     * 新增实例时判断生效
     *
     * @param label 标签信息
     * @param heartbeatMsg 心跳信息
     */
    private ScheduleLabelValidVo addData(JSONObject label, JSONObject heartbeatMsg) {
        ScheduleLabelValidVo labelValidVo = new ScheduleLabelValidVo();
        labelValidVo.setOn(label.getString(LabelConstant.VALID_MARKING));
        labelValidVo.setLabelName(label.getString(LabelConstant.LABEL_NAME_MARKING));
        labelValidVo.setLabelGroupName(label.getString(LabelConstant.LABEL_GROUP_NAME_MARKING));
        labelValidVo.setValue(label.getString(LabelConstant.VALUE_OF_LABEL));
        labelValidVo.setServiceName(heartbeatMsg.getString(LabelConstant.SERVICE_NAME_MARKING));
        labelValidVo.setInstanceName(heartbeatMsg.getString(LabelConstant.INSTANCE_NAME_MARKING));
        labelValidVo.setIp(heartbeatMsg.getString(LabelConstant.NETTY_IP));
        labelValidVo.setPort(heartbeatMsg.getInteger(LabelConstant.NETTY_PORT));
        return labelValidVo;
    }

    private boolean isHealthy(String heartbeatMsg) {
        try {
            return isHealthy(JSONObject.parseObject(heartbeatMsg));
        } catch (JSONException e) {
            LOGGER.error("HeartbeatMsg is invalid.", e);
            // 无效的心跳信息，当成不健康的实例
            return false;
        }
    }

    private boolean isHealthy(JSONObject heartbeatMsg) {
        long time = System.currentTimeMillis() - heartbeatMsg.getLongValue(LAST_HEARTBEAT);
        LOGGER.debug("time = {}", time);
        return time <= AgentHeartbeatConfig.getUnhealthyMachineMillis();
    }

    /**
     * 获取所有存储服务和其实例的集合，一对多，key为服务名，value为实例名
     *
     * @return 服务和其实例的集合
     */
    public Map<String, List<String>> getAllServicesMappedInstances() {
        return getAllServicesHeartbeat().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> {
            JSONObject heartbeat = JSONObject.parseObject(entry.getValue());
            return new ArrayList<>(heartbeat.keySet());
        }));
    }

    /**
     * 获取指定存储服务的实例名
     *
     * @param serviceName 服务名
     * @return 实例名
     */
    public List<String> getServiceMappedInstances(String serviceName) {
        JSONObject heartbeat = getServiceHeartbeat(serviceName);
        if (CollectionUtils.isEmpty(heartbeat)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(heartbeat.keySet());
    }

    /**
     * 获取指定服务以及实例心跳，key为实例名，value为心跳信息
     *
     * @param serviceName 服务名
     * @return 获取指定服务以及实例心跳
     */
    public JSONObject getServiceHeartbeat(String serviceName) {
        return getServiceHeartbeat(serviceName, true);
    }

    /**
     * 获取指定健康服务以及实例心跳，key为实例名，value为心跳信息
     *
     * @param serviceName 服务名
     * @param onlyHealthy 是否只需要健康的服务
     * @return 获取指定服务以及实例心跳
     */
    public JSONObject getServiceHeartbeat(String serviceName, boolean onlyHealthy) {
        String msg = redisUtil.getHash(HEARTBEAT_HASH_KEY, serviceName);
        if (StringUtils.isBlank(msg)) {
            return new JSONObject();
        }
        JSONObject heartbeat = JSONObject.parseObject(msg);
        if (!onlyHealthy) {
            return heartbeat;

        }
        // 过滤掉不健康的服务
        return new JSONObject(heartbeat.entrySet().stream()
                .filter(entry -> isHealthy((String) entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
    }

    /**
     * 获取所有健康的服务以及实例心跳，key为服务名，value为各实例的心跳信息
     *
     * @return 获取所有服务以及实例心跳
     */
    public Map<String, String> getAllServicesHeartbeat() {
        return getAllServicesHeartbeat(true);
    }

    /**
     * 获取所有服务以及实例心跳，key为服务名，value为各实例的心跳信息
     *
     * @param onlyHealthy 是否只需要健康的服务
     * @return 获取所有服务以及实例心跳
     */
    public Map<String, String> getAllServicesHeartbeat(boolean onlyHealthy) {
        Map<String, String> heartbeatMap = redisUtil.getHashMap(HEARTBEAT_HASH_KEY);
        if (CollectionUtils.isEmpty(heartbeatMap)) {
            return Collections.emptyMap();
        }
        if (!onlyHealthy) {
            return heartbeatMap;
        }
        Iterator<Entry<String, String>> heartbeatMapIterator = heartbeatMap.entrySet().iterator();
        while (heartbeatMapIterator.hasNext()) {
            Entry<String, String> heartbeatMapNext = heartbeatMapIterator.next();
            JSONObject heartbeat = JSONObject.parseObject(heartbeatMapNext.getValue());
            if (CollectionUtils.isEmpty(heartbeat)) {
                // 没有存在心跳的实例，过滤掉
                heartbeatMapIterator.remove();
                continue;
            }
            Iterator<Entry<String, Object>> iterator = heartbeat.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Object> next = iterator.next();
                if (isHealthy((String) next.getValue())) {
                    continue;
                }
                // 过滤掉不健康的服务
                iterator.remove();
            }
            if (CollectionUtils.isEmpty(heartbeat)) {
                // 没有存在心跳的实例，过滤掉
                heartbeatMapIterator.remove();
            } else {
                heartbeatMap.put(heartbeatMapNext.getKey(), heartbeat.toJSONString());
            }
        }
        return heartbeatMap;
    }

    /**
     * 验证实例启动时标签生效
     */
    @Scheduled(fixedDelay = PERIOD, initialDelay = INITIAL_DELAY)
    public void validateInstanceUpForLabelTakeEffect() {
        if (!redisUtil.setIfAbsent(LABEL_TAKE_EFFECT_REDIS_LOCK_KEY, "", LABEL_TAKE_EFFECT_REDIS_LOCK_TIMEOUT)) {
            return;
        }
        getAllServicesHeartbeat().entrySet().parallelStream().forEach(this::checkLabel);
        redisUtil.delKey(LABEL_TAKE_EFFECT_REDIS_LOCK_KEY);
    }

    private void checkLabel(Entry<String, String> entry) {
        String serviceName = entry.getKey();
        try {
            JSONObject heartbeat = JSONObject.parseObject(entry.getValue());
            if (CollectionUtils.isEmpty(heartbeat)) {
                return;
            }
            // 是否需要修改的标记
            AtomicBoolean modify = new AtomicBoolean();
            heartbeat.forEach((instanceName, msg) -> {
                JSONObject heartbeatMsg;
                try {
                    heartbeatMsg = JSONObject.parseObject((String) msg);
                } catch (Exception e) {
                    LOGGER.error("HeartbeatMsg is invalid.", e);
                    return;
                }
                // 更新映射表
                tagInstanceIpMapper.updateMapper(instanceName, heartbeatMsg);
                if (!heartbeatMsg.getBooleanValue(EFFECT_FLAG) && isHealthy(heartbeatMsg)) {
                    List<JSONObject> serviceLabels = labelService.instanceStartLabelValid(serviceName);
                    if (!CollectionUtils.isEmpty(serviceLabels)) {
                        boolean result = labelService.instanceStartValid(serviceLabels.stream().map(label ->
                                addData(label, heartbeatMsg)).collect(Collectors.toList()));
                        if (!result) {
                            return;
                        }
                    }
                    // 没有标签也直接设置为生效成功，服务启动之后的标签会通过标签的新增或生效进行推送，不需要经过这里推送
                    heartbeatMsg.put(EFFECT_FLAG, true);
                    heartbeat.put(instanceName, heartbeatMsg.toJSONString());
                    LOGGER.info("init instance is started. instanceName = {}", instanceName);
                    if (!modify.get()) {
                        modify.set(true);
                    }
                }
            });
            if (modify.get()) {
                setHeartbeat(serviceName, heartbeat);
            }
        } catch (Exception e) {
            LOGGER.error("Application startup, but label can't take effect.", e);
        }
    }

    /**
     * 验证心跳
     */
    @Scheduled(fixedDelay = HEARTBEAT_PERIOD, initialDelay = HEARTBEAT_INITIAL_DELAY)
    public void validateHeartbeatTask() {
        if (!redisUtil.setIfAbsent(HEARTBEAT_REDIS_LOCK_KEY, "", HEARTBEAT_REDIS_LOCK_TIMEOUT)) {
            return;
        }
        getAllServicesHeartbeat(false).entrySet().parallelStream().forEach(this::checkHeartbeat);
        redisUtil.delKey(HEARTBEAT_REDIS_LOCK_KEY);
    }

    private void checkHeartbeat(Entry<String, String> entry) {
        String serviceName = entry.getKey();
        try {
            JSONObject heartbeat = JSONObject.parseObject(entry.getValue());
            if (CollectionUtils.isEmpty(heartbeat)) {
                return;
            }
            // 是否需要修改的标记
            boolean modify = false;
            Iterator<Entry<String, Object>> iterator = heartbeat.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Object> next = iterator.next();
                if (isHealthy((String) next.getValue())) {
                    continue;
                }
                String instanceName = next.getKey();
                iterator.remove();
                labelService.deleteTempLabel(serviceName, instanceName);
                if (!modify) {
                    modify = true;
                }
                LOGGER.info("Instance [{}] stopped.", instanceName);
            }
            if (modify) {
                setHeartbeat(serviceName, heartbeat);
            }
        } catch (JSONException e) {
            LOGGER.error("validate heartbeat failed.", e);
            // 整个服务的心跳信息都是无效的，则删除缓存
            setHeartbeat(serviceName, null);
        }
    }

    private void setHeartbeat(String serviceName, JSONObject heartbeat) {
        if (CollectionUtils.isEmpty(heartbeat)) {
            // 没有实例存在心跳，则删除掉redis的值
            redisUtil.delHash(HEARTBEAT_HASH_KEY, serviceName);
        } else {
            redisUtil.setHash(HEARTBEAT_HASH_KEY, serviceName, heartbeat.toJSONString());
        }
    }
}
