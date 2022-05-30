/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.interceptors.health;

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.handler.SingleStateCloseHandler;
import com.huawei.registry.support.FieldAccessAction;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceWatch;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.logging.Logger;

/**
 * 注册中心健康状态变更
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class ZookeeperHealthInterceptor extends SingleStateCloseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private boolean isAvailable(TreeCacheEvent.Type eventType) {
        return eventType != TreeCacheEvent.Type.CONNECTION_LOST
            && eventType != TreeCacheEvent.Type.CONNECTION_SUSPENDED;
    }

    @Override
    protected void close() throws Exception {
        ZookeeperServiceWatch watch = (ZookeeperServiceWatch) target;
        final Field curator = watch.getClass().getDeclaredField("curator");
        AccessController.doPrivileged(new FieldAccessAction(curator));
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        AccessController.doPrivileged(new FieldAccessAction(modifiersField));
        final CuratorFramework client = (CuratorFramework) curator.get(target);

        // 关闭客户端, 停止定时器
        client.close();
        final Field cache = watch.getClass().getDeclaredField("cache");
        AccessController.doPrivileged(new FieldAccessAction(cache));
        modifiersField.setInt(cache, cache.getModifiers() & ~Modifier.FINAL);

        // 清空缓存
        cache.set(target, null);
        LOGGER.warning("Zookeeper client has been closed by user.");
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        checkState(context, null);
        if (arguments.length > 1 && arguments[1] instanceof TreeCacheEvent) {
            TreeCacheEvent event = (TreeCacheEvent) arguments[1];
            if (!isAvailable(event.getType())) {
                // 注册中心断开
                RegisterContext.INSTANCE.compareAndSet(true, false);
            } else if (isAvailable(event.getType())) {
                // 注册中心可用
                RegisterContext.INSTANCE.compareAndSet(false, true);
            } else {
                return context;
            }
        }
        return context;
    }
}
