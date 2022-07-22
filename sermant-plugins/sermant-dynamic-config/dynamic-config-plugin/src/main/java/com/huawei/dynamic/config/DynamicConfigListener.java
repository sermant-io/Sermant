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

package com.huawei.dynamic.config;

import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

/**
 * 动态配置监听器
 *
 * @author zhouss
 * @since 2022-04-13
 */
public interface DynamicConfigListener {
    /**
     * 通知刷新配置
     *
     * @param event 配置修改事件
     */
    void configChange(DynamicConfigEvent event);

    /**
     * 优先级
     *
     * @return 执行优先级
     */
    default int getOrder() {
        return 0;
    }
}
