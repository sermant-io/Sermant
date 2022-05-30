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

package com.huawei.registry.handler;

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.support.RegisterSwitchSupport;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * 关闭注册中心处理
 *
 * @author zhouss
 * @since 2022-01-04
 */
public abstract class SingleStateCloseHandler extends RegisterSwitchSupport {
    /**
     * 注册中心是否关闭
     */
    protected static final AtomicBoolean IS_CLOSED = new AtomicBoolean();

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 增强对象
     */
    protected Object target;

    /**
     * 方法参数
     */
    protected Object[] arguments;

    /**
     * 构造器
     */
    public SingleStateCloseHandler() {
        RegisterContext.INSTANCE.registerCloseHandler(this);
    }

    /**
     * 关闭注册中心
     */
    protected void tryClose() {
        if (IS_CLOSED.compareAndSet(false, true)) {
            try {
                close();
                RegisterContext.INSTANCE.setAvailable(false);
            } catch (Exception ex) {
                // 重置状态
                resetCloseState();
                LOGGER.warning(String.format(Locale.ENGLISH,
                    "Closed register healthy check failed! %s", ex.getMessage()));
            }
        }
    }

    /**
     * 判断注册中心状态, 若需关闭调用关闭心跳方法, 并用指定结果跳过
     *
     * @param context 增强上下文
     * @param result  指定结果
     */
    protected void checkState(ExecuteContext context, Object result) {
        setArguments(context.getArguments());
        setTarget(context.getObject());
        if (needCloseRegisterCenter()) {
            context.skip(result);
            tryClose();
        }
    }

    /**
     * 重置开关状态 当某个注册中心关闭失败需要重新关闭时可调用
     */
    private void resetCloseState() {
        IS_CLOSED.set(false);
    }

    /**
     * 关闭注册中心
     *
     * @throws Exception 关闭失败时抛出
     */
    protected abstract void close() throws Exception;

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }
}
