/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.core.service.heartbeat.api;

import java.util.Map;

/**
 * Information Specifies the additional information provider. If the content sent by the heartbeat is changed,
 * need to customize the information provision mode
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-29
 */
public interface ExtInfoProvider {
    /**
     * Provides a collection of additional information
     *
     * @return additional information
     */
    Map<String, String> getExtInfo();
}
