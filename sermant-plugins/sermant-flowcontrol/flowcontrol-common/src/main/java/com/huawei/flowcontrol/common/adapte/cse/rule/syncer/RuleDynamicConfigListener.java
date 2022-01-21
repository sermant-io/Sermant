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

package com.huawei.flowcontrol.common.adapte.cse.rule.syncer;

import com.huawei.flowcontrol.common.adapte.cse.ResolverManager;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 规则同步监听器
 *
 * @author zhouss
 * @since 2022-01-25
 */
public class RuleDynamicConfigListener implements DynamicConfigListener {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void process(DynamicConfigEvent event) {
        ResolverManager.INSTANCE.resolve(event.getKey(), event.getContent(),
                event.getEventType() == DynamicConfigEventType.DELETE);
        LOGGER.log(Level.INFO, String.format(Locale.ENGLISH, "Config [%s] has been %s ", event.getKey(),
                event.getEventType()));
    }
}
