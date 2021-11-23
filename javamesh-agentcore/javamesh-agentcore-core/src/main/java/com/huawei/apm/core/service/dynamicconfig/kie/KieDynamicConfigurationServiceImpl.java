/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.service.dynamicconfig.kie;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.dynamicconfig.Config;
import com.huawei.apm.core.service.dynamicconfig.kie.kie.KieRequest;
import com.huawei.apm.core.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationService;

import java.util.logging.Logger;

/**
 * kie配置中心实现
 * key生成规则：基于KieRequest实体类生成，例如需要
 *      <p>1.创建标签version:1.0的监听
 *         <p>KieRequestFactory.buildKieRequest(new String[]{KieRequestFactory.buildLabel("version", "1.0")});</p>
 *      <p>2.创建标签version:1.0并打开长连接监听,20S一个周期（最大不可超过50S）</p>
 *        <p>KieRequestFactory.buildKieRequest(20, new String[]{KieRequestFactory.buildLabel("version", "1.0")});</p>
 *<p></p>
 * @author zhouss
 * @since 2021-11-22
 */
public class KieDynamicConfigurationServiceImpl implements DynamicConfigurationService {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static SubscriberManager subscriberManager;

    private static KieDynamicConfigurationServiceImpl instance;

    private final KeyHandler<KieRequest> kieRequestKeyHandler = new DefaultKeyHandler<KieRequest>();

    /**
     * 获取实现单例
     *
     * @return KieDynamicConfigurationService
     */
    public static synchronized KieDynamicConfigurationServiceImpl getInstance() {
        if (instance == null) {
            instance = new KieDynamicConfigurationServiceImpl();
            subscriberManager = new SubscriberManager(Config.getInstance().getKieUrl());
        }
        return instance;
    }

    @Override
    public boolean addListener(String key, String group, ConfigurationListener listener) {
        return updateListener(key, group, listener, true);
    }

    @Override
    public boolean removeListener(String key, String group, ConfigurationListener listener) {
        return updateListener(key, group, listener, false);
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

    private boolean updateListener(String key, String group, ConfigurationListener listener, boolean forSubscribe) {
        final KieRequest kieRequest = kieRequestKeyHandler.handle(key, KieRequest.class);
        if (kieRequest == null || listener == null) {
            return false;
        }
        try {
            if (forSubscribe) {
                subscriberManager.subscribe(kieRequest, listener);
            } else {
                subscriberManager.unSubscribe(kieRequest, listener);
            }

        } catch (Exception exception) {
            LOGGER.warning("Format kie request failed! raw json : " + key);
            return false;
        }
        return true;
    }

    public interface KeyHandler<R> {
        /**
         * key处理
         *
         * @param key 键
         * @param clazz 响应类型
         * @return 处理后结果
         */
        R handle(String key, Class<R> clazz);
    }

    /**
     * 默认JSON实现
     *
     * @param <R> 结果类型
     */
    static class DefaultKeyHandler<R> implements KeyHandler<R> {
        @Override
        public R handle(String key, Class<R> clazz) {
            if (key == null || clazz == null) {
                return null;
            }
            try {
                return JSONObject.parseObject(key, clazz);
            } catch (JSONException ex) {
                LOGGER.warning("Format key failed! raw json key is : " + key);
            }
            return null;
        }
    }
}
