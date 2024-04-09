/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.core.rule.fault;

import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.common.core.constants.RuleConstants;
import com.huawei.flowcontrol.common.core.rule.AbstractRule;
import com.huawei.flowcontrol.common.util.StringUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * fault injection
 *
 * @author zhouss
 * @since 2022-08-05
 */
public class FaultRule extends AbstractRule {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * error injection type currently supported
     * delay
     * abort
     */
    private String type = RuleConstants.FAULT_RULE_DELAY_TYPE;

    /**
     * the default delay time is 0
     */
    private String delayTime = "0";

    /**
     * the delay time after resolution
     */
    private long parsedDelayTime;

    /**
     * injection probability
     */
    private int percentage = -1;

    /**
     * error code when throwing an exception
     */
    private int errorCode = CommonConst.INTERVAL_SERVER_ERROR;

    /**
     * Whether to force shutdown. When set to true, it never takes effect
     */
    private boolean forceClosed = false;

    /**
     * Feedback type: The following two types are supported
     * ThrowException: direct throw exception
     * ReturnNull: return null directly
     */
    private String fallbackType = RuleConstants.FAULT_RULE_FALLBACK_THROW_TYPE;

    @Override
    public boolean isInValid() {
        if (parsedDelayTime < 0) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "Fault rule is not valid, delay time must gt 0, but it now [%s]", parsedDelayTime));
            return true;
        }
        if (!StringUtils.equal(RuleConstants.FAULT_RULE_ABORT_TYPE, this.type)
                && !StringUtils.equal(RuleConstants.FAULT_RULE_DELAY_TYPE, this.type)) {
            return true;
        }
        if (!StringUtils.equal(RuleConstants.FAULT_RULE_FALLBACK_NULL_TYPE, this.fallbackType)
                && !StringUtils.equal(RuleConstants.FAULT_RULE_FALLBACK_THROW_TYPE, this.fallbackType)) {
            return true;
        }
        return super.isInValid();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDelayTime() {
        return delayTime;
    }

    /**
     * set the delay time and convert it to long
     *
     * @param delayTime delay time
     */
    public void setDelayTime(String delayTime) {
        this.delayTime = delayTime;
        this.parsedDelayTime = super.parseLongTime(delayTime, 0L);
    }

    public long getParsedDelayTime() {
        return parsedDelayTime;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isForceClosed() {
        return forceClosed;
    }

    public void setForceClosed(boolean forceClosed) {
        this.forceClosed = forceClosed;
    }

    public String getFallbackType() {
        return fallbackType;
    }

    public void setFallbackType(String fallbackType) {
        this.fallbackType = fallbackType;
    }
}
