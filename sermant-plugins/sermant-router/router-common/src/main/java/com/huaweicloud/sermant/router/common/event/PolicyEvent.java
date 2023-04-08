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

package com.huaweicloud.sermant.router.common.event;

/**
 * Policy事件包含三件：
 * 1.相同标签规则匹配成功: 全部实例最小可用阈值大于全部可用实例数，则同TAG优先
 * 2.相同标签规则匹配成功:
 *     未设置全部实例最小可用阈值，超过(大于等于)同TAG比例阈值，则同TAG优先
 *     设置了全部实例最小可用阈值，但其小于全部TAG可用实例，超过(大于等于)同TAG比例阈值，则同TAG优先
 * 3.相同标签规则匹配失败
 *
 * @author robotLJW
 * @since 2023-04-03
 *
 */
public enum PolicyEvent {
    /**
     * 符合相同Tag规则匹配：全部可用实例数小于全部实例最小可用阈值，则同TAG优先
     */
    SAME_TAG_MATCH_LESS_MIN_ALL_INSTANCES("According to the policy in the rule, same tag rule match that less"
            + " than the minimum available threshold for all instances"),

    /**
     * 符合相同Tag规则匹配：超过同TAG比例阈值，则同TAG优先
     */
    SAME_TAG_MATCH_EXCEEDED_TRIGGER_THRESHOLD("According to the policy in the rule, same tag rule match that"
            + " exceeded trigger threshold"),

    /**
     * 相同Tag规则匹配没匹配上
     */
    SAME_TAG_MISMATCH("According to the policy in the rule, same tag rule mismatch");

    /**
     * 事件描述
     */
    private String desc;

    PolicyEvent(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }
}
