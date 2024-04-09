/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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
 * The Policy event consists of three pieces:
 * 1.If the same tag rule is successfully matched, the minimum available threshold of all instances is greater than the
 * number of all available instances
 * 2.The same tag rule is successfully matched:
 *     If the minimum availability threshold for all instances is not set and exceeds (greater than or equal to) the
 *     threshold of the same TAG, the same TAG takes precedence
 *     If the minimum available threshold for all instances is set, but it is less than all TAG available instances and
 *     exceeds (greater than or equal to) the threshold of the same TAG, the same TAG takes precedence
 * 3.Failed to match the same tag rule
 *
 * @author robotLJW
 * @since 2023-04-03
 *
 */
public enum PolicyEvent {
    /**
     * If the same tag rule matches: If the number of all available instances is less than the minimum available
     * threshold of all instances, the same TAG takes precedence
     */
    SAME_TAG_MATCH_LESS_MIN_ALL_INSTANCES("According to the policy in the rule, same tag rule match that less"
            + " than the minimum available threshold for all instances"),

    /**
     * Matching matches that meet the same tag rules:
     * If the ratio of the same TAG exceeds the threshold, the same TAG takes precedence
     */
    SAME_TAG_MATCH_EXCEEDED_TRIGGER_THRESHOLD("According to the policy in the rule, same tag rule match that"
            + " exceeded trigger threshold"),

    /**
     * The same tag rule does not match
     */
    SAME_TAG_MISMATCH("According to the policy in the rule, same tag rule mismatch");

    /**
     * description of the event
     */
    private String desc;

    PolicyEvent(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }
}
