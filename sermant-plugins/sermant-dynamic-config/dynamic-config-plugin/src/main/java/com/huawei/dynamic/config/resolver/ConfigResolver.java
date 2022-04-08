/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.dynamic.config.resolver;

import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

/**
 * 配置解析器
 *
 * @param <T> 解析后的類型
 * @author zhouss
 * @since 2022-04-13
 */
public interface ConfigResolver<T> {
    /**
     * 配置解析器
     *
     * @param event 配置更新時
     * @return 解析数据
     */
    T resolve(DynamicConfigEvent event);
}
