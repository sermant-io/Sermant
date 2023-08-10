/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.service.inject.config;

import com.huaweicloud.sermant.core.config.common.BaseConfig;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;

import java.util.Collections;
import java.util.Set;

/**
 * 类注入核心服务配置,
 *
 * @author luanwenfei
 * @since 2023-08-10
 */
@ConfigTypeKey("inject")
public class InjectConfig implements BaseConfig {
    /**
     * 在类注入服务使用时，指定注入的类所需不可或缺的包，将会通过对类加载的增强来辅助获取这些包中的类
     */
    private Set<String> essentialPackage = Collections.singleton("com.huaweicloud.sermant");

    public Set<String> getEssentialPackage() {
        return essentialPackage;
    }

    public void setEssentialPackage(Set<String> essentialPackage) {
        this.essentialPackage = essentialPackage;
    }
}
