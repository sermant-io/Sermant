/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.service.dynamicconfig.kie;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huawei.apm.core.service.dynamicconfig.kie.utils.KieGroupUtils;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * kie配置中心实现
 * <p></p>
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class KieDynamicConfigurationServiceImpl implements DynamicConfigurationService {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static SubscriberManager subscriberManager;

    private static KieDynamicConfigurationServiceImpl instance;

    private final Map<String, List<String>> groupKeyCache = new ConcurrentHashMap<String, List<String>>();

    private KieDynamicConfigurationServiceImpl() {

    }

    /**
     * 获取实现单例
     *
     * @return KieDynamicConfigurationService
     */
    public static synchronized KieDynamicConfigurationServiceImpl getInstance() {
        if (instance == null) {
            instance = new KieDynamicConfigurationServiceImpl();
//            subscriberManager = new SubscriberManager(Config.getInstance().getKieUrl());
            subscriberManager = new SubscriberManager("http://172.31.100.55:30110");
        }
        return instance;
    }

    @Override
    public boolean removeGroupListener(String key, String group, ConfigurationListener listener) {
        return updateListener("GroupKey", group, listener, false);
    }

    @Override
    public boolean addGroupListener(String group, ConfigurationListener listener) {
        return updateListener("GroupKey", group, listener, true);
    }

    @Override
    public boolean addConfigListener(String key, String group, ConfigurationListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeConfigListener(String key, String group, ConfigurationListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getConfig(String key, String group) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean publishConfig(String key, String group, String content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultGroup() {
        return "java-mesh";
    }

    @Override
    public long getDefaultTimeout() {
        return 0;
    }

    @Override
    public List<String> listConfigsFromGroup(String group) throws Exception {
        return groupKeyCache.get(group);
    }

    /**
     * 更新监听器（删除||添加）
     *
     * @param key          监听键
     * @param group        分组， 针对KIE特别处理生成group方法{@link KieGroupUtils#createLabelGroup(Map)}
     * @param listener     对应改组的监听器
     * @param forSubscribe 是否为订阅
     * @return 更新是否成功
     */
    private boolean updateListener(String key, String group, ConfigurationListener listener, boolean forSubscribe) {
        updateGroupKey(key, group, forSubscribe);
        try {
            if (forSubscribe) {
                return subscriberManager.addGroupListener(group, listener);
            } else {
                return subscriberManager.removeGroupListener(group, listener);
            }
        } catch (Exception exception) {
            LOGGER.warning("Subscribed kie request failed! raw key : " + key);
            return false;
        }
    }

    private void updateGroupKey(String key, String group, boolean forSubscribe) {
        List<String> keys = groupKeyCache.get(group);
        if (keys == null) {
            keys = new ArrayList<>();
        }
        if (forSubscribe) {
            keys.remove(key);
        } else {
            keys.add(key);
        }
        groupKeyCache.put(group, keys);
    }
}
