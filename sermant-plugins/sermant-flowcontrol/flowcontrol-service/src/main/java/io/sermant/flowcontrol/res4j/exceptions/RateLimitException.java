/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.flowcontrol.res4j.exceptions;

import io.sermant.core.service.xds.entity.XdsHeaderOption;

import java.util.List;

/**
 * rate limit injection exception
 *
 * @author zhp
 * @since 2024-12-05
 */
public class RateLimitException extends RuntimeException {
    private List<XdsHeaderOption> xdsHeaderOptions;

    /**
     * Constructor
     *
     * @param xdsHeaderOptions Header name/value pair plus option
     */
    public RateLimitException(List<XdsHeaderOption> xdsHeaderOptions) {
        this.xdsHeaderOptions = xdsHeaderOptions;
    }

    public List<XdsHeaderOption> getXdsHeaderOptions() {
        return xdsHeaderOptions;
    }

    public void setXdsHeaderOptions(List<XdsHeaderOption> xdsHeaderOptions) {
        this.xdsHeaderOptions = xdsHeaderOptions;
    }
}
