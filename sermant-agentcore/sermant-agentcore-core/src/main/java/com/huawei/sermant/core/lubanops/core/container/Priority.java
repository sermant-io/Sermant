/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.lubanops.core.container;

/**
 *
 * @author
 */
public interface Priority {

    int HIGHEST = Integer.MIN_VALUE;

    int LOWEST = Integer.MAX_VALUE;

    int DEFAULT = 100;

    int AGENT_INTERNAL_SERVICE = 10;

    int AGENT_INTERNAL_INFRASTRUCTURE_SERVICE = 0;

    int EARLY_EXPOSE = -1;

    /**
     * get priority.
     *
     * @return
     */
    int getPriority();
}
