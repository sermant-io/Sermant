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

package com.huawei.sermant.core.service.dynamicconfig.kie;

import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huawei.sermant.core.service.dynamicconfig.kie.client.kie.KieConfigEntity;
import com.huawei.sermant.core.service.dynamicconfig.kie.client.kie.KieResponse;
import com.huawei.sermant.core.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huawei.sermant.core.service.dynamicconfig.utils.LabelGroupUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * kie配置中心实现
 * <p></p>
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class KieDynamicConfigService extends DynamicConfigService {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static SubscriberManager subscriberManager;

    public KieDynamicConfigService() {
        subscriberManager = new SubscriberManager(CONFIG.getServerAddress());
    }

    public KieDynamicConfigService(String serverAddress, String project) {
        subscriberManager = new SubscriberManager(serverAddress, project);
    }

    @Override
    protected boolean doRemoveGroupListener(String group) {
        return updateGroupListener(group, null, false, false);
    }

    @Override
    protected boolean doAddGroupListener(String group, DynamicConfigListener listener) {
        return addGroupListener(group, listener, false);
    }

    @Override
    public boolean addGroupListener(String group, DynamicConfigListener listener, boolean ifNotify) {
        return updateGroupListener(group, listener, true, ifNotify);
    }

    @Override
    protected boolean doAddConfigListener(String key, String group, DynamicConfigListener listener) {
        return addConfigListener(key, group, listener, false);
    }

    @Override
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener, boolean ifNotify) {
        if (!LabelGroupUtils.isLabelGroup(group)) {
            // 增加标签group判断, 对不规则的group进行适配处理
            group = LabelGroupUtils.createLabelGroup(
                    Collections.singletonMap(fixSeparator(group, true), fixSeparator(key, false)));
        }
        return subscriberManager.addConfigListener(key, group, listener, ifNotify);
    }

    @Override
    protected boolean doRemoveConfigListener(String key, String group) {
        // 不支持移除单个监听器
        return false;
    }

    @Override
    protected String doGetConfig(String key, String group) {
        final KieResponse kieResponse = subscriberManager.queryConfigurations(null,
                LabelGroupUtils.getLabelCondition(group));
        if (!isValidResponse(kieResponse)) {
            return null;
        }
        final List<KieConfigEntity> data = kieResponse.getData();
        for (KieConfigEntity entity : data) {
            if (key.equals(entity.getKey())) {
                return entity.getValue();
            }
        }
        return null;
    }

    @Override
    protected boolean doPublishConfig(String key, String group, String content) {
        return subscriberManager.publishConfig(key, group, content);
    }

    @Override
    protected boolean doRemoveConfig(String key, String group) {
        return subscriberManager.removeConfig(key, group);
    }

    @Override
    protected List<String> doListKeysFromGroup(String group) {
        final KieResponse kieResponse = subscriberManager.queryConfigurations(null,
                LabelGroupUtils.getLabelCondition(group));
        if (isValidResponse(kieResponse)) {
            final List<KieConfigEntity> data = kieResponse.getData();
            final List<String> keys = new ArrayList<String>(data.size());
            for (KieConfigEntity entity : data) {
                keys.add(entity.getKey());
            }
            return keys;
        }
        return Collections.emptyList();
    }

    private boolean isValidResponse(KieResponse kieResponse) {
        return kieResponse != null && kieResponse.getData() != null;
    }

    /**
     * 更新监听器（删除||添加）
     * 若第一次添加监听器，则会将数据通知给监听器
     *
     * @param group        分组， 针对KIE特别处理生成group方法{@link LabelGroupUtils#createLabelGroup(Map)}
     * @param listener     对应改组的监听器
     * @param forSubscribe 是否为订阅
     * @param ifNotify     初次添加监听器，是否通知监听的数据
     * @return 更新是否成功
     */
    private synchronized boolean updateGroupListener(String group, DynamicConfigListener listener,
                                                     boolean forSubscribe, boolean ifNotify) {
        if (listener == null) {
            LOGGER.warning("Empty listener is not allowed. ");
            return false;
        }
        try {
            if (forSubscribe) {
                return subscriberManager.addGroupListener(fixGroup(group), listener, ifNotify);
            } else {
                return subscriberManager.removeGroupListener(fixGroup(group), listener);
            }
        } catch (Exception exception) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Subscribed kie request failed! raw group : %s", group));
            return false;
        }
    }

    /**
     * 去除路径分隔符
     *
     * @param str     key or group
     * @param isGroup 是否为组
     * @return 修正值
     */
    private String fixSeparator(String str, boolean isGroup) {
        if (str == null) {
            if (isGroup) {
                // 默认分组
                str = CONFIG.getDefaultGroup();
            } else {
                throw new IllegalArgumentException("Key must not be empty!");
            }
        }
        return str.startsWith("/") ? str.substring(1) : str;
    }
}
