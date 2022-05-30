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
 * 注册中心动态配置
 *
 * @author zhouss
 * @since 2021-12-30
 */
public class RegisterDynamicConfig {
    /**
     * 单例
     */
    public static final RegisterDynamicConfig INSTANCE = new RegisterDynamicConfig();

    /**
     * 停用原有的注册中心开关 关联动态配置, 由用户配置下发, 默认不开启
     */
    private boolean needClose = false;

    RegisterDynamicConfig() {
    }

    public boolean isNeedCloseOriginRegisterCenter() {
        return needClose;
    }

    public void setClose(boolean isNeedCloseOriginRegisterCenter) {
        this.needClose = isNeedCloseOriginRegisterCenter;
    }
}
