/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.service.lb;

import com.huawei.discovery.config.LbConfig;

/**
 * 负载均衡常量
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class LbConstants {
    /**
     * 注册标记
     */
    public static final String SERMANT_DISCOVERY = "sermant-discovery";

    /**
     * 分钟转为秒的单位
     */
    public static final int MIN_TO_SEC = 60;

    /**
     * 秒转为毫秒的单位
     */
    public static final int SEC_TO_MS = 1000;

    /**
     * 实例过期前多久开始刷新实例列表
     */
    public static final long GAP_MS_BEFORE_EXPIRE_MS = 10000L;

    /**
     * {@link LbConfig#getInstanceCacheExpireTime()} * 1000需大于MIN_GAP_MS_BEFORE_EXPIRE_SEC
     * 才执行GAP_MS_BEFORE_EXPIRE_SEC的策略
     */
    public static final long MIN_GAP_MS_BEFORE_EXPIRE_MS = 50000L;

    /**
     * 若刷新时间小于GAP_MS_BEFORE_EXPIRE_SEC, 则采用浮点计算, 在0.1*{@link LbConfig#getInstanceCacheExpireTime()} * 1000之前刷新实例列表
     */
    public static final float GAP_MS_BEFORE_EXPIRE_DELTA = 0.1f;

    private LbConstants() {
    }
}
