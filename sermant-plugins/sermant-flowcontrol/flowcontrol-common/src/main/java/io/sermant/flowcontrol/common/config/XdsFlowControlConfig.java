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

package io.sermant.flowcontrol.common.config;

import io.sermant.core.config.common.ConfigFieldKey;
import io.sermant.core.config.common.ConfigTypeKey;
import io.sermant.core.plugin.config.PluginConfig;

import java.util.List;

/**
 * retry configuration class
 *
 * @author zhouss
 * @since 2022-01-28
 */
@ConfigTypeKey("xds.flow.control.config")
public class XdsFlowControlConfig implements PluginConfig {
    /**
     * Specify the response code for retry, and retry will be executed when the response code is included
     */
    @ConfigFieldKey("x-sermant-retriable-status-codes")
    private List<String> retryStatusCodes;

    /**
     * Specify the response code for retry, and retry will be executed when the response header is included
     */
    @ConfigFieldKey("x-sermant-retriable-header-names")
    private List<String> retryHeaderNames;

    /**
     * xds flow control switch
     */
    private boolean enable;

    public List<String> getRetryStatusCodes() {
        return retryStatusCodes;
    }

    public void setRetryStatusCodes(List<String> retryStatusCodes) {
        this.retryStatusCodes = retryStatusCodes;
    }

    public List<String> getRetryHeaderNames() {
        return retryHeaderNames;
    }

    public void setRetryHeaderNames(List<String> retryHeaderNames) {
        this.retryHeaderNames = retryHeaderNames;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
