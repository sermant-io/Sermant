/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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
 * 错误注入
 *
 * @author zhouss
 * @since 2022-08-05
 */
public class FaultRule extends AbstractRule {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 错误注入类型,当前支持
     * delay : 延迟
     * abort : 丢弃
     */
    private String type = RuleConstants.FAULT_RULE_DELAY_TYPE;

    /**
     * 延迟时间, 默认0
     */
    private String delayTime = "0";

    /**
     * 解析后的延迟时间
     */
    private long parsedDelayTime;

    /**
     * 注入概率
     */
    private int percentage = -1;

    /**
     * 抛异常时错误码
     */
    private int errorCode = CommonConst.INTERVAL_SERVER_ERROR;

    /**
     * 是否强制关闭, 设置为true时, 永远不会生效
     */
    private boolean forceClosed = false;

    /**
     * 反馈类型,当前支持以下两种
     * ThrowException: 直接抛异常
     * ReturnNull: 直接返回null
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
     * 设置延迟时间, 并转换为long
     *
     * @param delayTime 延迟时间
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
