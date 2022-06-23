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
import com.huawei.registry.utils.ReflectUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import com.ecwid.consul.v1.Response;

import org.springframework.cloud.consul.discovery.ConsulCatalogWatch;
import org.springframework.scheduling.config.ScheduledTask;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 注册中心健康状态变更
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class ConsulHealthInterceptor extends SingleStateCloseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 仅在2.x.x以上的版本采用该方式停止
     * <p></p>
     * 1.x.x版本直接阻止catalogServicesWatch方逻辑调用 参考类{@link ConsulWatchRequestInterceptor}
     */
    @Override
    protected void close() {
        // 关闭consul心跳发送
        final Object registerWatch = RegisterContext.INSTANCE.getRegisterWatch();
        if ((registerWatch instanceof ConsulCatalogWatch) && canStopTask(registerWatch)) {
            ConsulCatalogWatch watch = (ConsulCatalogWatch) registerWatch;
            watch.stop();
            LOGGER.warning("Consul heartbeat has been closed.");
        } else {
            // 通过定时器的方式关闭consul心跳, 仅在1.x.x存在该场景
            final Object scheduleProcessor = RegisterContext.INSTANCE.getScheduleProcessor();
            final Optional<Object> scheduledTasks = ReflectUtils.getFieldValue(scheduleProcessor, "scheduledTasks");
            if (!scheduledTasks.isPresent()) {
                return;
            }
            final Object tasks = scheduledTasks.get();
            if (tasks instanceof Map) {
                Map<Object, Set<ScheduledTask>> convertTasks = (Map<Object, Set<ScheduledTask>>) tasks;
                final Set<ScheduledTask> heartBeatTasks =
                        convertTasks.remove(RegisterContext.INSTANCE.getRegisterWatch());
                for (ScheduledTask scheduledTask : heartBeatTasks) {
                    scheduledTask.cancel();
                }
                LOGGER.warning("Consul heartbeat has been closed by stopping scheduled task.");
            }
        }
    }

    private boolean canStopTask(Object watch) {
        try {
            watch.getClass().getDeclaredMethod("stop");
            return true;
        } catch (NoSuchMethodException ex) {
            LOGGER.info(String.format(Locale.ENGLISH,
                "Consul register center version is less than 2.x.x, it has not method named stop"
                    + " it will be replaced by stop scheduled task of catalogServicesWatch! %s", ex.getMessage()));
        }
        return false;
    }

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        checkState(context, new Response<Map<String, List<String>>>(Collections.emptyMap(), null, null, null));
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        final Object result = context.getResult();
        if (result != null) {
            // 原始注册中心恢复
            RegisterContext.INSTANCE.compareAndSet(false, true);
        }
        return context;
    }

    @Override
    public ExecuteContext doThrow(ExecuteContext context) {
        // 请求注册中心失败说明注册中心已失联
        RegisterContext.INSTANCE.compareAndSet(true, false);
        return context;
    }
}
