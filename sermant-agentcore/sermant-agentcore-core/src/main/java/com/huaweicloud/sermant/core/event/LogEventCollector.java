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

package com.huaweicloud.sermant.core.event;

import com.huaweicloud.sermant.core.event.common.Event;
import com.huaweicloud.sermant.core.event.common.EventCollector;

/**
 * 日志事件采集器
 *
 * @author luanwenfei
 * @since 2023-03-04
 */
public class LogEventCollector extends EventCollector {
    @Override
    public boolean offerEvent(Event event) {
        /**
         * todo 判断当前事件类型5min内是否上报过 如果上报过 就压缩 统计个数
         */

        /**
         * todo 判断该收集器中的事件缓存是否已满，满了则主动上报后再添加
         */
        return super.offerEvent(event);
    }
}