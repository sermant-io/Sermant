/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.sermant.core.plugin.subscribe.processor;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huawei.sermant.core.utils.MapUtils;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;
import org.yaml.snakeyaml.representer.Representer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 多个标签场景, 根据指定优先级集中处理, 基于优先级覆盖配置顺序
 *
 * @author zhouss
 * @since 2022-04-22
 */
public class ConfigOrderIntegratedProcessor implements ConfigProcessor {
    /**
     * map初始化容量
     */
    private static final int CAP_SIZE = 8;

    private final Yaml yaml;

    /**
     * 原监听器
     */
    private final DynamicConfigListener originListener;

    /**
     * 配置持有
     */
    private List<ConfigDataHolder> dataHolders;

    /**
     * 构造器
     *
     * @param listener 原始监听器
     */
    public ConfigOrderIntegratedProcessor(DynamicConfigListener listener) {
        this.originListener = listener;
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        this.yaml = new Yaml(representer);
    }

    /**
     * 添加数据持有器
     *
     * @param dataHolder 数据持有器
     */
    @Override
    public final void addHolder(ConfigDataHolder dataHolder) {
        if (this.dataHolders == null) {
            this.dataHolders = new ArrayList<>(CAP_SIZE);
        }
        this.dataHolders.add(dataHolder);
        Collections.sort(dataHolders);
    }

    @Override
    public final void process(String rawGroup, DynamicConfigEvent event) {
        final Optional<ConfigDataHolder> targetHolder = findTargetHolder(rawGroup);
        synchronized (this) {
            originListener.process(targetHolder.map(dataHolder -> rebuildEvent(dataHolder, event))
                .orElse(event));
        }
    }

    /**
     * 重构事件
     *
     * @param targetHolder 目标数据持有器
     * @param originEvent  原始事件
     * @return DynamicConfigEvent
     */
    private DynamicConfigEvent rebuildEvent(ConfigDataHolder targetHolder, DynamicConfigEvent originEvent) {
        if (updateHolder(targetHolder, originEvent)) {
            return new OrderConfigEvent(originEvent.getKey(), originEvent.getGroup(),
                yaml.dump(buildOrderData(originEvent)), originEvent.getEventType(), buildOrderData());
        }
        return originEvent;
    }

    /**
     * 构建按照优先级覆盖的数据
     *
     * @return orderData
     */
    private Map<String, Object> buildOrderData() {
        final Map<String, Object> result = new HashMap<>(CAP_SIZE);
        for (ConfigDataHolder dataHolder : dataHolders) {
            for (Map<String, Object> data : dataHolder.getHolder().values()) {
                // 为避免多个层次配置相互覆盖, 此处直接将全键名解析出来
                final HashMap<String, Object> resolveData = new HashMap<>(data.size());
                MapUtils.resolveNestMap(resolveData, data, null);
                result.putAll(resolveData);
            }
        }
        return result;
    }

    private Map<String, Object> buildOrderData(DynamicConfigEvent originEvent) {
        final Map<String, Object> result = new HashMap<>(CAP_SIZE);
        dataHolders.forEach(dataHolder -> {
            final Map<String, Object> curContent = dataHolder.getHolder().get(originEvent.getKey());
            if (curContent != null) {
                final HashMap<String, Object> resolveData = new HashMap<>(curContent.size());
                MapUtils.resolveNestMap(resolveData, curContent, null);
                result.putAll(resolveData);
            }
        });
        return result;
    }

    private boolean updateHolder(ConfigDataHolder targetHolder, DynamicConfigEvent originEvent) {
        final Map<String, Object> olderDataMap = targetHolder.getHolder()
            .getOrDefault(originEvent.getKey(), new HashMap<>(CAP_SIZE));
        olderDataMap.clear();
        if (originEvent.getEventType() != DynamicConfigEventType.DELETE) {
            Map<String, Object> dataMap;
            try {
                dataMap = yaml.loadAs(originEvent.getContent(), Map.class);
            } catch (ConstructorException ex) {
                LoggerFactory.getLogger().warning(String.format(Locale.ENGLISH,
                    "Can not load key [%s], raw data: [%s], reason: [%s]",
                    originEvent.getKey(), originEvent.getContent(), ex.getMessage()));
                return false;
            }
            olderDataMap.putAll(dataMap);
        }
        targetHolder.getHolder().put(originEvent.getKey(), olderDataMap);
        return true;
    }

    private Optional<ConfigDataHolder> findTargetHolder(String group) {
        return dataHolders.stream().filter(dataHolder -> StringUtils.equals(dataHolder.getGroup(), group))
            .findAny();
    }

    /**
     * 优先级排序事件, 附带全量数据
     *
     * @since 2022-04-21
     */
    public static class OrderConfigEvent extends DynamicConfigEvent {
        private static final long serialVersionUID = 4990176887738080367L;

        private final Map<String, Object> allData;

        /**
         * 构造器
         *
         * @param key       配置键
         * @param group     组
         * @param content   配置内容
         * @param eventType 事件类型
         * @param allData   所有数据
         */
        public OrderConfigEvent(String key, String group, String content, DynamicConfigEventType eventType, Map<String,
            Object> allData) {
            super(key, group, content, eventType);
            this.allData = allData;
        }

        /**
         * 全量数据
         *
         * @return 全量接收的数据, 已按照优先级进行数据覆盖
         */
        public Map<String, Object> getAllData() {
            return this.allData;
        }
    }
}
