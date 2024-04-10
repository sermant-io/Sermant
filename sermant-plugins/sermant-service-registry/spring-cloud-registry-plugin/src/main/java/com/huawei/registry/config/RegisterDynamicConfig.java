/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.config;

/**
 * Dynamic configuration of the registry
 *
 * @author zhouss
 * @since 2021-12-30
 */
public class RegisterDynamicConfig {
    /**
     * Singleton
     */
    public static final RegisterDynamicConfig INSTANCE = new RegisterDynamicConfig();

    /**
     * Disable the original registry switch Associated dynamic configuration, which is delivered by user configuration
     * and is not enabled by default
     */
    private boolean needClose = false;

    RegisterDynamicConfig() {
    }

    /**
     * Whether the original registry needs to be closed
     *
     * @return Whether a closed identity is required
     */
    public boolean isNeedCloseOriginRegisterCenter() {
        return needClose;
    }

    /**
     * Set whether you need to turn off the identity of the original registry
     *
     * @param isNeedCloseOriginRegisterCenter Whether you need to turn off the identity of the original registry
     */
    public void setClose(boolean isNeedCloseOriginRegisterCenter) {
        this.needClose = isNeedCloseOriginRegisterCenter;
    }
}
