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

package io.sermant.registry.handler;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.registry.context.RegisterContext;
import io.sermant.registry.support.RegisterSwitchSupport;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Close registry processing
 *
 * @author zhouss
 * @since 2022-01-04
 */
public abstract class SingleStateCloseHandler extends RegisterSwitchSupport {
    /**
     * Whether the registry is closed
     */
    protected static final AtomicBoolean IS_CLOSED = new AtomicBoolean();

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Enhance the object
     */
    protected Object target;

    /**
     * Method parameters
     */
    protected Object[] arguments;

    /**
     * Constructor
     */
    public SingleStateCloseHandler() {
        RegisterContext.INSTANCE.registerCloseHandler(this);
    }

    /**
     * Close the registry
     */
    protected void tryClose() {
        if (IS_CLOSED.compareAndSet(false, true)) {
            try {
                close();
                RegisterContext.INSTANCE.setAvailable(false);
            } catch (Exception ex) {
                // Reset the state
                resetCloseState();
                LOGGER.warning(String.format(Locale.ENGLISH,
                        "Closed register healthy check failed! %s", ex.getMessage()));
            }
        }
    }

    /**
     * Determine the status of the registry, call the close heartbeat method if you need to close, and skip it with the
     * specified result
     *
     * @param context Enhance context
     * @param result Specify the result
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
     * Reset switch state: It can be called when a registry fails to close and needs to be closed again
     */
    private void resetCloseState() {
        IS_CLOSED.set(false);
    }

    /**
     * Close the registry
     *
     * @throws Exception Thrown when closing fails
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
